package com.github.dakusui.actionunit.visitors;

import com.github.dakusui.actionunit.actions.*;
import com.github.dakusui.actionunit.compat.Context;
import com.github.dakusui.actionunit.compat.actions.CompatWhen;
import com.github.dakusui.actionunit.compat.actions.CompatWhile;
import com.github.dakusui.actionunit.compat.connectors.Connectors;
import com.github.dakusui.actionunit.compat.visitors.CompatActionRunner;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.AutocloseableIterator;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.helpers.Autocloseables;
import com.github.dakusui.actionunit.helpers.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

import static com.github.dakusui.actionunit.helpers.Checks.checkArgument;
import static com.github.dakusui.actionunit.helpers.Checks.propagate;
import static com.github.dakusui.actionunit.helpers.Utils.runWithTimeout;
import static com.github.dakusui.actionunit.helpers.Utils.sleep;
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
public abstract class ActionRunner extends CompatActionRunner implements Action.Visitor {
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
  public void visit(Sequential sequential) {
    try (AutocloseableIterator<Action> i = sequential.iterator()) {
      while (i.hasNext()) {
        toRunnable(i.next()).run();
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(Concurrent concurrent) {
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

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> void visit(ForEach<T> forEach) {
    StreamSupport.stream(forEach.data().spliterator(), forEach.getMode() == ForEach.Mode.CONCURRENTLY)
        .map((T item) -> (Supplier<T>) () -> item)
        .map(forEach::createHandler)
        .forEach((Action eachChild) -> {
          eachChild.accept(this);
        });
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
  public void visit(TestAction test) {
    test.given().accept(this);
    test.when().accept(this);
    test.then().accept(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> void visit(CompatWhile<T> while$) {
    //noinspection unchecked
    while (while$.test((T) this.value())) {
      while$.getAction().accept(this);
    }
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(CompatWhen when) {
    //noinspection unchecked
    if (when.test(this.value())) {
      when.getAction().accept(this);
    } else {
      when.otherwise().accept(this);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(Retry retry) {
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
   * An extension point to allow users to customize how a concurrent action will be
   * executed by this {@code Visitor}.
   *
   * @param action An action executed by a runnable object returned by this method.
   */
  protected Iterable<Callable<Boolean>> toCallables(Concurrent action) {
    return toCallables(toRunnables(action));
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