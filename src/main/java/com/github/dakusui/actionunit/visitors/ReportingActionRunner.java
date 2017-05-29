package com.github.dakusui.actionunit.visitors;

import com.github.dakusui.actionunit.actions.*;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.AutocloseableIterator;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.helpers.Autocloseables;
import com.github.dakusui.actionunit.helpers.Checks;
import com.github.dakusui.actionunit.helpers.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.github.dakusui.actionunit.helpers.Checks.propagate;
import static com.github.dakusui.actionunit.helpers.Utils.runWithTimeout;
import static com.github.dakusui.actionunit.helpers.Utils.sleep;
import static java.lang.Integer.max;
import static java.lang.Integer.min;
import static java.lang.String.format;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.stream.StreamSupport.stream;

public class ReportingActionRunner extends ActionWalker implements Action.Visitor {
  private final int                     threadPoolSize;
  private final Writer                  writer;
  private final Report.Record.Formatter formatter;
  private final Report                  report;

  public static class Builder {
    private static final int DEFAULT_THREAD_POOL_SIZE = 5;
    private final Action action;
    private int                     threadPoolSize = DEFAULT_THREAD_POOL_SIZE;
    private Report.Record.Formatter formatter      = Report.Record.Formatter.DEFAULT_INSTANCE;
    private Writer                  writer         = Writer.Std.OUT;


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

    public Builder setThreadPoolSize(int threadPoolSize) {
      Checks.checkArgument(threadPoolSize > 0);
      this.threadPoolSize = threadPoolSize;
      return this;
    }

    public ReportingActionRunner build() {
      return new ReportingActionRunner(threadPoolSize, TreeBuilder.traverse(action), writer, formatter);
    }
  }

  private ReportingActionRunner(int threadPoolSize, Node<Action> tree, Writer writer, Report.Record.Formatter formatter) {
    this.threadPoolSize = threadPoolSize;
    this.report = new Report(tree);
    this.writer = writer;
    this.formatter = formatter;
  }

  public void perform() {
    Objects.requireNonNull(formatter);
    try {
      this.report.root.getContent().accept(this);
    } finally {
      Node.walk(
          this.report.root,
          (actionNode, nodes) -> writer.writeLine(
              formatter.format(actionNode, this.report.get(actionNode), nodes.size())
          ));
    }
  }

  @Override
  public void visit(Action action) {
    throw new UnsupportedOperationException(Utils.describe(action));
  }

  @Override
  public void visit(Leaf action) {
    handle(
        action,
        Leaf::perform
    );
  }

  @Override
  public void visit(Named action) {
    handle(
        action,
        (Named named) -> named.getAction().accept(this)
    );
  }

  @Override
  public void visit(Sequential action) {
    handle(
        action,
        (Sequential sequential) -> {
          try (AutocloseableIterator<Action> i = sequential.iterator()) {
            while (i.hasNext()) {
              i.next().accept(this);
            }
          }
        }
    );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(Concurrent action) {
    handle(
        action,
        (Concurrent concurrent) -> {
          final ExecutorService pool = newFixedThreadPool(min(this.threadPoolSize, max(1, Utils.toList(concurrent).size())));
          try {
            Iterator<Callable<Boolean>> i = toCallables(concurrent).iterator();
            //noinspection unused
            try (AutoCloseable resource = Autocloseables.toAutocloseable(i)) {
              List<Future<Boolean>> futures = new ArrayList<>(this.threadPoolSize);
              while (i.hasNext()) {
                futures.add(pool.submit(i.next()));
                if (futures.size() == this.threadPoolSize || !i.hasNext()) {
                  for (Future<Boolean> each : futures) {
                    ////
                    // Unless accessing the returned value of Future#get(), compiler may
                    // optimize execution and the action may not be executed even if this loop
                    // has ended.
                    //noinspection unused
                    each.get();
                  }
                }
              }
            } catch (ExecutionException e) {
              if (e.getCause() instanceof Error) {
                throw (Error) e.getCause();
              }
              ////
              // It's safe to cast to RuntimeException, because checked exception cannot
              // be thrown from inside Runnable#run()
              throw (RuntimeException) e.getCause();
            } catch (Exception e) {
              // InterruptedException should be handled by this clause, too.
              throw ActionException.wrap(e);
            }
          } finally {
            pool.shutdownNow();
          }
        }
    );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> void visit(ForEach<T> action) {
    handle(
        action,
        (ForEach<T> forEach) -> stream(forEach.data().spliterator(), forEach.getMode() == ForEach.Mode.CONCURRENTLY)
            .map((T item) -> (Supplier<T>) () -> item)
            .map(forEach::createHandler)
            .forEach((Action eachChild) -> {
              eachChild.accept(ReportingActionRunner.this);
            })
    );
  }

  /**
   * {@inheritDoc}
   */
  public <T> void visit(While<T> action) {
    handle(
        action,
        (While<T> while$) -> {
          Supplier<T> value = while$.value();
          //noinspection unchecked
          while (while$.check().test(value.get())) {
            while$.createHandler(value).accept(ReportingActionRunner.this);
          }
        }
    );
  }

  /**
   * {@inheritDoc}
   */
  public <T> void visit(When<T> action) {
    handle(
        action,
        (When<T> when) -> {
          Supplier<T> value = when.value();
          //noinspection unchecked
          if (when.check().test(value.get())) {
            when.perform(value).accept(ReportingActionRunner.this);
          } else {
            when.otherwise(value).accept(ReportingActionRunner.this);
          }
        }
    );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T extends Throwable> void visit(Attempt<T> action) {
    handle(
        action,
        (Attempt<T> attempt) -> {
          try {
            attempt.attempt().accept(this);
          } catch (Throwable e) {
            if (!attempt.exceptionClass().isAssignableFrom(e.getClass())) {
              throw propagate(e);
            }
            //noinspection unchecked
            attempt.recover(() -> (T) e).accept(this);
          } finally {
            attempt.ensure().accept(this);
          }
        }
    );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(TestAction action) {
    handle(
        action,
        (TestAction test) -> {
          test.given().accept(this);
          test.when().accept(this);
          test.then().accept(this);
        }
    );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(Retry action) {
    handle(
        action,
        (Retry retry) -> {
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
        }
    );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(final TimeOut action) {
    runWithTimeout((Callable<Object>) () -> {
          action.action.accept(ReportingActionRunner.this);
          return true;
        },
        action.durationInNanos,
        NANOSECONDS
    );
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

  /**
   * An extension point to allow users to customize how a concurrent action will be
   * executed by this {@code Visitor}.
   *
   * @param action An action executed by a runnable object returned by this method.
   */
  private Iterable<Callable<Boolean>> toCallables(Concurrent action) {
    return toCallables(toRunnables(action));
  }

  private Iterable<Runnable> toRunnables(final Iterable<? extends Action> actions) {
    return Autocloseables.transform(
        actions,
        (Function<Action, Runnable>) this::toRunnable
    );
  }

  private Runnable toRunnable(final Action action) {
    return () -> action.accept(ReportingActionRunner.this);
  }

  private Iterable<Callable<Boolean>> toCallables(final Iterable<Runnable> runnables) {
    return Autocloseables.transform(
        runnables,
        input -> (Callable<Boolean>) () -> {
          input.run();
          return true;
        }
    );
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
}
