package com.github.dakusui.actionunit.visitors;

import com.github.dakusui.actionunit.actions.*;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.helpers.Checks;
import com.github.dakusui.actionunit.helpers.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.github.dakusui.actionunit.helpers.Utils.runWithTimeout;
import static com.github.dakusui.actionunit.helpers.Utils.sleep;
import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.stream.StreamSupport.stream;

public class ReportingActionRunner extends ActionWalker implements Action.Visitor {
  private final Writer                  writer;
  private final Report.Record.Formatter formatter;
  private final Report                  report;

  public static class Builder {
    private final Action action;
    private Report.Record.Formatter formatter = Report.Record.Formatter.DEFAULT_INSTANCE;
    private Writer                  writer    = Writer.Std.OUT;


    public Builder(Action action) {
      this.action = Objects.requireNonNull(action);
    }

    public Builder with(Report.Record.Formatter formatter) {
      this.formatter = Objects.requireNonNull(formatter);
      return this;
    }

    public Builder to(Writer writer) {
      this.writer = Objects.requireNonNull(writer);
      return this;
    }

    public ReportingActionRunner build() {
      return new ReportingActionRunner(TreeBuilder.traverse(action), writer, formatter);
    }
  }

  private ReportingActionRunner(Node<Action> tree, Writer writer, Report.Record.Formatter formatter) {
    this.report = new Report(tree);
    this.writer = writer;
    this.formatter = formatter;
  }

  public void perform() {
    Objects.requireNonNull(formatter);
    try {
      this.report.root.getContent().accept(this);
    } catch (Wrapped e) {
      throw Checks.propagate(e.getCause());
    } finally {
      Node.walk(
          this.report.root,
          (actionNode, nodes) -> writer.writeLine(
              formatter.format(actionNode, this.report.get(actionNode), nodes.size())
          ));
    }
  }

  /**
   * {@inheritDoc}
   */
  public <T> void visit(While<T> action) {
    handle(
        action,
        whileActionConsumer()
    );
  }

  /**
   * {@inheritDoc}
   */
  public <T> void visit(When<T> action) {
    handle(
        action,
        whenActionConsumer()
    );
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public <T extends Throwable> void visit(Attempt<T> action) {
    handle(
        action,
        attemptActionConsumer()
    );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(TestAction action) {
    handle(
        action,
        testActionConsumer()
    );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(Retry action) {
    handle(
        action,
        retryActionConsumer()
    );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(final TimeOut action) {
    handle(
        action,
        timeOutActionConsumer(action));
  }

  @Override
  Consumer<Concurrent> concurrentActionConsumer() {
    return (Concurrent concurrent) -> {
      Deque<Node<Action>> pathSnapshot = snapshotCurrentPath();
      StreamSupport.stream(concurrent.spliterator(), false)
          .map(this::toRunnable)
          .map((Runnable runnable) -> (Runnable) () -> {
            branchPath(pathSnapshot);
            runnable.run();
          })
          .collect(Collectors.toList())
          .parallelStream()
          .forEach(Runnable::run);
    };
  }

  @Override
  Consumer<Leaf> leafActionConsumer() {
    return Leaf::perform;
  }

  @Override
  <T> Consumer<ForEach<T>> forEachActionConsumer() {
    return (ForEach<T> forEach) -> stream(forEach.data().spliterator(), forEach.getMode() == ForEach.Mode.CONCURRENTLY)
        .map((T item) -> (Supplier<T>) () -> item)
        .map(forEach::createHandler)
        .forEach((Action eachChild) -> {
          eachChild.accept(ReportingActionRunner.this);
        });
  }

  private <T> Consumer<While<T>> whileActionConsumer() {
    return (While<T> while$) -> {
      Supplier<T> value = while$.value();
      //noinspection unchecked
      while (while$.check().test(value.get())) {
        while$.createHandler(value).accept(ReportingActionRunner.this);
      }
    };
  }

  private <T> Consumer<When<T>> whenActionConsumer() {
    return (When<T> when) -> {
      Supplier<T> value = when.value();
      //noinspection unchecked
      if (when.check().test(value.get())) {
        when.perform(value).accept(ReportingActionRunner.this);
      } else {
        when.otherwise(value).accept(ReportingActionRunner.this);
      }
    };
  }

  private <T extends Throwable> Consumer<Attempt<T>> attemptActionConsumer() {
    return (Attempt<T> attempt) -> {
      try {
        attempt.attempt().accept(this);
      } catch (Throwable e) {
        if (!attempt.exceptionClass().isAssignableFrom(e.getClass())) {
          throw new Wrapped(e);
        }
        //noinspection unchecked
        attempt.recover(() -> (T) e).accept(this);
      } finally {
        attempt.ensure().accept(this);
      }
    };
  }

  private Consumer<TestAction> testActionConsumer() {
    return (TestAction test) -> {
      test.given().accept(this);
      test.when().accept(this);
      test.then().accept(this);
    };
  }

  private Consumer<Retry> retryActionConsumer() {
    return (Retry retry) -> {
      try {
        toRunnable(retry.action).run();
      } catch (Throwable e) {
        Throwable lastException = e;
        for (int i = 0; i < retry.times || retry.times == Retry.INFINITE; i++) {
          if (retry.getTargetExceptionClass().isAssignableFrom(lastException.getClass())) {
            sleep(retry.intervalInNanos, NANOSECONDS);
            try {
              toRunnable(retry.action).run();
              return;
            } catch (Throwable t) {
              lastException = t;
            }
          } else {
            throw ActionException.wrap(lastException);
          }
        }
        throw ActionException.wrap(lastException);
      }
    };
  }

  private Consumer<TimeOut> timeOutActionConsumer(TimeOut action) {
    return timeOut -> {
      Deque<Node<Action>> snapshotPath = snapshotCurrentPath();
      runWithTimeout((Callable<Object>) () -> {
            branchPath(snapshotPath);
            action.action.accept(ReportingActionRunner.this);
            return true;
          },
          action.durationInNanos,
          NANOSECONDS
      );
    };
  }

  private void branchPath(Deque<Node<Action>> pathSnapshot) {
    ReportingActionRunner.this._current.set(new LinkedList<>(pathSnapshot));
  }

  private LinkedList<Node<Action>> snapshotCurrentPath() {
    return new LinkedList<>(this.getCurrentPath());
  }

  @Override
  <A extends Action> Node<A> toNode(Node<Action> parent, A action) {
    if (parent == null) {
      //noinspection unchecked
      return (Node<A>) this.report.root;
    }
    //noinspection unchecked
    return (Node<A>) parent.children().stream(
    ).filter(
        (Node<Action> n) -> checker().test(n, super.toNode(parent, action))
    ).collect(
        Utils.singletonCollector(
            () -> new IllegalStateException(
                format(
                    "More than one node matching '%s' were found under '%s'(%s). Consider using 'named' action for them.",
                    Utils.describe(action),
                    parent,
                    childrenToString(parent)
                )))
    ).orElseThrow(
        () -> new IllegalStateException(
            format(
                "Node matching '%s' was not found under '%s'(%s)",
                Utils.describe(action),
                parent,
                childrenToString(parent)
            ))
    );
  }

  private String childrenToString(Node<?> parent) {
    return String.join(
        ",",
        parent.children().stream()
            .map(n -> n.getContent().toString())
            .collect(Collectors.toList())
    );
  }

  private <A extends Node<?>> BiPredicate<A, A> checker() {
    return (a, b) ->
        Objects.equals(a, b)
            || Objects.equals(a.getContent(), b.getContent())
            || Objects.equals(Utils.describe(a.getContent()), Utils.describe(b.getContent()));
  }

  @SuppressWarnings("unchecked")
  <A extends Action> void notFinished(Node<A> node) {
    this.report.notFinished((Node<Action>) node);
  }

  @SuppressWarnings("unchecked")
  <A extends Action> void succeeded(Node<A> node) {
    this.report.succeeded((Node<Action>) node);
  }

  @SuppressWarnings("unchecked")
  <A extends Action> void failed(Node<A> node, Throwable e) {
    this.report.failed((Node<Action>) node, e);
  }

  private Runnable toRunnable(final Action action) {
    return () -> action.accept(ReportingActionRunner.this);
  }

  /**
   * An interface that abstracts various destinations to which {@link ActionPrinter.Impl}'s
   * output goes.
   */
  public interface Writer {
    void writeLine(String s);

    class Impl implements Writer, Iterable<String> {
      List<String> arr = new ArrayList<>();

      @Override
      public void writeLine(String s) {
        arr.add(s);
      }

      @Override
      public Iterator<String> iterator() {
        return this.arr.iterator();
      }
    }

    enum Std implements Writer {
      OUT {
        @Override
        public void writeLine(String s) {
          System.out.println(s);
        }
      },
      ERR {
        @Override
        public void writeLine(String s) {
          System.err.println(s);
        }
      };

      @Override
      public abstract void writeLine(String s);
    }

    enum Slf4J implements Writer {
      TRACE {
        @Override
        public void writeLine(String s) {
          LOGGER.trace(s);
        }
      },
      DEBUG {
        @Override
        public void writeLine(String s) {
          LOGGER.debug(s);
        }
      },
      INFO {
        @Override
        public void writeLine(String s) {
          LOGGER.info(s);
        }
      },
      WARN {
        @Override
        public void writeLine(String s) {
          LOGGER.warn(s);
        }
      },
      ERROR {
        @Override
        public void writeLine(String s) {
          LOGGER.error(s);
        }
      };
      private static final Logger LOGGER = LoggerFactory.getLogger(Writer.Slf4J.class);
    }
  }

  static class Wrapped extends RuntimeException {
    private Wrapped(Throwable t) {
      super(t);
    }
  }
}
