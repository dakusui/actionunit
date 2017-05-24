package com.github.dakusui.actionunit.visitors;

import com.github.dakusui.actionunit.compat.CompatActionRunnerWithResult;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.AutocloseableIterator;
import com.github.dakusui.actionunit.helpers.Autocloseables;
import com.github.dakusui.actionunit.compat.Context;
import com.github.dakusui.actionunit.actions.*;
import com.github.dakusui.actionunit.compat.actions.*;
import com.github.dakusui.actionunit.compat.connectors.Connectors;
import com.github.dakusui.actionunit.compat.connectors.Sink;
import com.github.dakusui.actionunit.compat.connectors.Source;
import com.github.dakusui.actionunit.exceptions.ActionException;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.github.dakusui.actionunit.helpers.Checks.*;
import static com.github.dakusui.actionunit.helpers.Utils.*;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.String.format;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * A simple visitor that invokes actions.
 * Typically, an instance of this class will be applied to a given action in a following manner.
 * <p/>
 * <code>
 * action.accept(new ActionRunner.Impl());
 * </code>
 *
 * @see ActionRunner.Impl
 */
public abstract class ActionRunner extends Action.Visitor.Base implements Action.Visitor, Context {
  private static final int DEFAULT_THREAD_POOL_SIZE = 5;
  private final int threadPoolSize;

  /**
   * Creates an object of this class.
   *
   * @param threadPoolSize Size of thread pool used to execute concurrent actions.
   * @see ActionRunner#ActionRunner(int)
   */
  public ActionRunner(int threadPoolSize) {
    checkArgument(threadPoolSize > 0, "Thread pool size must be larger than 0 but %s was given.", threadPoolSize);
    this.threadPoolSize = threadPoolSize;
  }

  /**
   * Creates an object of this class with {@code DEFAULT_THREAD_POOL_SIZE}.
   */
  public ActionRunner() {
    this(DEFAULT_THREAD_POOL_SIZE);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(Action action) {
    throw new UnsupportedOperationException(format("Unsupported action type '%s'", action.getClass().getCanonicalName()));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(Leaf action) {
    action.perform();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(Named action) {
    action.getAction().accept(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(Sequential action) {
    try (AutocloseableIterator<Action> i = action.iterator()) {
      while (i.hasNext()) {
        toRunnable(i.next()).run();
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(Concurrent action) {
    final ExecutorService pool = newFixedThreadPool(min(this.threadPoolSize, max(1, toList(action).size())));
    try {
      Iterator<Callable<Boolean>> i = toCallables(action).iterator();
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

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(CompatForEach action) {
    action.getElements(this).accept(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> void visit(ForEach<T> action) {
    action.getCompositeFactory().create(
        StreamSupport.stream(action.data().spliterator(), false)
            .map(
                item -> action.createProcessor(() -> item)).collect(Collectors.toList()))
        .accept(this);
  }

  @Override
  public <E extends Throwable> void visit(Attempt<E> action) {
    try {
      action.attempt().accept(this);
    } catch (Throwable e) {
      if (!action.exceptionClass().isAssignableFrom(e.getClass())) {
        throw propagate(e);
      }
      //noinspection unchecked
      action.recover(() -> (E) e).accept(this);
    } finally {
      action.ensure().accept(this);
    }
  }

  @Override
  public void visit(TestAction action) {
    action.given().accept(this);
    action.when().accept(this);
    action.then().accept(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(While action) {
    //noinspection unchecked
    while (action.test(this.value())) {
      action.getAction().accept(this);
    }
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public <T> void visit(final CompatWith<T> action) {
    action.getAction().accept(createChildFor(action));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(When action) {
    //noinspection unchecked
    if (action.test(this.value())) {
      action.getAction().accept(this);
    } else {
      action.otherwise().accept(this);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(Retry action) {
    try {
      toRunnable(action.action).run();
    } catch (Throwable e) {
      Throwable lastException = e;
      for (int i = 0; i < action.times || action.times == Retry.INFINITE; i++) {
        if (action.getTargetExceptionClass().isAssignableFrom(lastException.getClass())) {
          sleep(action.intervalInNanos, NANOSECONDS);
          try {
            toRunnable(action.action).run();
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

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(final TimeOut action) {
    runWithTimeout((Callable<Object>) () -> {
          action.action.accept(ActionRunner.this);
          return true;
        },
        action.durationInNanos,
        NANOSECONDS
    );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(final CompatAttempt action) {
    try {
      action.attempt.accept(this);
    } catch (Throwable e) {
      //noinspection unchecked
      if (!action.exceptionClass.isAssignableFrom(e.getClass())) {
        throw propagate(e);
      }
      //noinspection unchecked
      new CompatActionRunnerWithResult.IgnoredInPathCalculation.With<>(Connectors.toSource(e), action.recover, action.sinks).accept(this);
    } finally {
      action.ensure.accept(this);
    }
  }

  /**
   * Subclasses of this class must override this method and return a subclass of
   * it whose {@code visit(Action.With.Tag)} is overridden.
   * And the method must call {@code acceptTagAction(Action.With.Tag, Action.With, ActionRunner)}.
   * <p/>
   * <code>
   * {@literal @}Override
   * public void visit(Action.With.Tag tagAction) {
   * acceptTagAction(tagAction, action, this);
   * }
   * </code>
   *
   * @param action action for which the returned Visitor is created.
   */
  protected <T> ActionRunner createChildFor(final CompatWith<T> action) {
    return new ActionRunner() {
      @Override
      public ActionRunner getParent() {
        return ActionRunner.this;
      }

      @Override
      public Object value() {
        //noinspection unchecked
        return action.getSource().apply(ActionRunner.this);
      }

      @Override
      public void visit(Tag tagAction) {
        acceptTagAction(tagAction, action, this);
      }
    };
  }

  /**
   * An extension point to allow users to customize how a concurrent action will be
   * executed by this {@code Visitor}.
   *
   * @param action An action executed by a runnable object returned by this method.
   */
  protected Iterable<Callable<Boolean>> toCallables(Concurrent action) {
    return toCallables(toRunnables(action));
  }

  protected static <T> void acceptTagAction(Tag tagAction, CompatWith<T> withAction, ActionRunner runner) {
    tagAction.toLeaf(withAction.getSource(), withAction.getSinks(), runner).accept(runner);
  }

  private Iterable<Runnable> toRunnables(final Iterable<? extends Action> actions) {
    return Autocloseables.transform(
        actions,
        (Function<Action, Runnable>) this::toRunnable
    );
  }

  private Runnable toRunnable(final Action action) {
    return () -> action.accept(ActionRunner.this);
  }

  protected Iterable<Callable<Boolean>> toCallables(final Iterable<Runnable> runnables) {
    return Autocloseables.transform(
        runnables,
        input -> (Callable<Boolean>) () -> {
          input.run();
          return true;
        }
    );
  }

  /**
   * This interface is used to suppress path calculation, which is
   * performed by {@link CompatActionRunnerWithResult}
   * and its printer.
   */
  public interface IgnoredInPathCalculation {
    abstract class Composite implements com.github.dakusui.actionunit.actions.Composite, IgnoredInPathCalculation {
      final com.github.dakusui.actionunit.actions.Composite inner;

      public Composite(com.github.dakusui.actionunit.actions.Composite inner) {
        this.inner = inner;
      }

      @Override
      public int size() {
        return inner.size();
      }

      @Override
      public AutocloseableIterator<Action> iterator() {
        return inner.iterator();
      }

      public static <T extends Composite> T create(com.github.dakusui.actionunit.actions.Composite composite) {
        Composite ret;
        if (composite instanceof com.github.dakusui.actionunit.actions.Sequential) {
          ret = new Sequential((com.github.dakusui.actionunit.actions.Sequential) composite);
        } else if (composite instanceof com.github.dakusui.actionunit.actions.Concurrent) {
          ret = new Concurrent((com.github.dakusui.actionunit.actions.Concurrent) composite);
        } else {
          throw new ActionException(format("Unknown type of composite action was given: %s", describe(composite)));
        }
        //noinspection unchecked
        return (T) ret;
      }
    }

    /**
     * A sequential action created by and run as a part of {@code CompatForEach} action.
     *
     * @see IgnoredInPathCalculation
     */
    class Sequential extends Composite implements com.github.dakusui.actionunit.actions.Sequential {
      public Sequential(com.github.dakusui.actionunit.actions.Sequential sequential) {
        super(sequential);
      }

      @Override
      public void accept(Visitor visitor) {
        visitor.visit(this);
      }
    }

    class Concurrent extends Composite implements com.github.dakusui.actionunit.actions.Concurrent {
      public Concurrent(com.github.dakusui.actionunit.actions.Concurrent concurrent) {
        super(concurrent);
      }

      @Override
      public void accept(Visitor visitor) {
        visitor.visit(this);
      }
    }

    /**
     * A "with" action created by and run as a part of {@code CompatForEach} action.
     *
     * @param <U> Type of the value with which child {@code Action} is executed.
     */
    class With<U> extends CompatWithBase<U> implements IgnoredInPathCalculation {
      public With(Source<U> source, Action action, Sink<U>[] sinks) {
        super(source, action, sinks);
      }
    }
  }

  /**
   * A simple implementation of an {@link ActionRunner}.
   */
  public static class Impl extends ActionRunner {
    /**
     * Creates an object of this class.
     */
    public Impl() {
      this(DEFAULT_THREAD_POOL_SIZE);
    }

    public Impl(int threadPoolSize) {
      super(threadPoolSize);
    }

    /**
     * Â
     * Returns {@code null} since this action runner is a top level one and
     * doesn't have any parent.
     * Subclasses of this class may override this method to return a meaningful
     * object.
     */
    @Override
    public Context getParent() {
      return null;
    }

    /**
     * Throws an {@link UnsupportedOperationException} since this action runner
     * doesn't have a context value.
     * Subclasses of this class may override this method to return a meaningful
     * object.
     */
    @Override
    public Object value() {
      return Connectors.INVALID;
    }
  }

}