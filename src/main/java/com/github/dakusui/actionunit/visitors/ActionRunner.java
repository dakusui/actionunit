package com.github.dakusui.actionunit.visitors;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.ActionException;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import java.util.concurrent.*;

import static com.github.dakusui.actionunit.Utils.runWithTimeout;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Math.min;
import static java.util.concurrent.Executors.newFixedThreadPool;

/**
 * A simple visitor that invokes actions.
 * Typically, an instance of this class will be applied to a given action in a following manner.
 *
 * <code>
 *   action.accept(new ActionRunner());
 * </code>
 *
 */
public class ActionRunner extends Action.Visitor.Base implements Action.Visitor {
  public static final int DEFAULT_THREAD_POOL_SIZE = 5;
  private final int threadPoolSize;

  public ActionRunner(int threadPoolSize) {
    checkArgument(threadPoolSize > 0, "Thread pool size must be larger than 0 but %s was given.", threadPoolSize);
    this.threadPoolSize = threadPoolSize;
  }

  public ActionRunner() {
    this(DEFAULT_THREAD_POOL_SIZE);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(Action action) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(Action.Leaf action) {
    action.perform();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(Action.Sequential action) {
    for (Action each : action.actions) {
      toTask(each).run();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(Action.Concurrent action) {
    final ExecutorService pool = newFixedThreadPool(min(this.threadPoolSize, size(action.actions)));
    try {
      for (final Future<Boolean> future : pool.invokeAll(newArrayList(toTasks(action.actions)))) {
        ////
        // Unless accessing the returned value of Future#get(), compiler may
        // optimize execution and the action may not be executed even if this loop
        // has ended.
        //noinspection unused
        boolean value = future.get();
      }
    } catch (InterruptedException e) {
      pool.shutdownNow();
      Thread.currentThread().interrupt();
    } catch (ExecutionException e) {
      if (e.getCause() instanceof Error) {
        throw (Error) e.getCause();
      }
      ////
      // It's safe to cast to RuntimeException, because checked exception cannot
      // be thrown from inside Runnable#run()
      throw (RuntimeException) e.getCause();
    } finally {
      while (!pool.isShutdown()) {
        pool.shutdown();
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(Action.Retry action) {
    try {
      toTask(action.action).run();
    } catch (ActionException e) {
      ActionException lastException = e;
      for (int i = 0; i < action.times; i++) {
        try {
          TimeUnit.NANOSECONDS.sleep(action.intervalInNanos);
          toTask(action.action).run();
          return;
        } catch (ActionException ee) {
          lastException = ee;
        } catch (InterruptedException ee) {
          throw new ActionException(ee);
        }
        toTask(action.action).run();
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> void visit(Action.RepeatIncrementally<T> action) {
    for (T each : action.dataSource) {
      toTask(action.factoryForActionWithTarget.create(each)).run();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(final Action.TimeOut action) {
    runWithTimeout(new Callable<Object>() {
                     @Override
                     public Object call() throws Exception {
                       action.action.accept(ActionRunner.this);
                       return true;
                     }
                   },
        action.durationInNanos,
        TimeUnit.NANOSECONDS
    );
  }

  /**
   * An extension point to allow users to customize how an action will be
   * executed by this {@code Visitor}.
   *
   * @param action An action executed by a runnable object returned by this method.
   */
  @SuppressWarnings("WeakerAccess")
  protected Runnable toTask(final Action action) {
    return new Runnable() {
      @Override
      public void run() {
        action.accept(ActionRunner.this);
      }
    };
  }

  private Iterable<Callable<Boolean>> toTasks(final Iterable<? extends Action> actions) {
    return Iterables.transform(
        actions,
        new Function<Action, Callable<Boolean>>() {
          @Override
          public Callable<Boolean> apply(final Action input) {
            return new Callable<Boolean>() {
              @Override
              public Boolean call() throws ActionException {
                toTask(input).run();
                return true;
              }
            };
          }
        }
    );
  }
}