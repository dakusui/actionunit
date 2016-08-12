package com.github.dakusui.actionunit.visitors;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.ActionException;
import com.google.common.base.Function;

import java.util.concurrent.*;

import static com.github.dakusui.actionunit.Utils.runWithTimeout;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Math.min;
import static java.util.concurrent.Executors.newFixedThreadPool;

/**
 * A simple visitor that invokes actions.
 * Typically, an instance of this class will be applied to a given action in a following manner.
 * <p/>
 * <code>
 * action.accept(new ActionRunner());
 * </code>
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
    for (Action each : action) {
      toRunnable(each).run();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(Action.Concurrent action) {
    final ExecutorService pool = newFixedThreadPool(min(this.threadPoolSize, size(action)));
    try {
      for (final Future<Boolean> future : pool.invokeAll(newArrayList(toCallables(toRunnables(action))))) {
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

  @Override
  public void visit(Action.ForEach action) {
    action.getElements().accept(this);
  }

  @Override
  public void visit(final Action.With action) {
    action.getAction().accept(new ActionRunner() {
      @Override
      public void visit(Action.With.Tag tagAction) {
        tagAction.toLeaf(action.value(), action.getBlocks()).accept(this);
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(Action.Retry action) {
    try {
      toRunnable(action.action).run();
    } catch (ActionException e) {
      ActionException lastException = e;
      for (int i = 0; i < action.times; i++) {
        try {
          TimeUnit.NANOSECONDS.sleep(action.intervalInNanos);
          toRunnable(action.action).run();
          return;
        } catch (ActionException ee) {
          lastException = ee;
        } catch (InterruptedException ee) {
          throw new ActionException(ee);
        }
      }
      throw lastException;
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
  protected Runnable toRunnable(final Action action) {
    return new Runnable() {
      @Override
      public void run() {
        action.accept(ActionRunner.this);
      }
    };
  }

  private Iterable<Runnable> toRunnables(final Iterable<? extends Action> actions) {
    return transform(
        actions,
        new Function<Action, Runnable>() {
          @Override
          public Runnable apply(final Action input) {
            return toRunnable(input);
          }
        }
    );
  }

  private Iterable<Callable<Boolean>> toCallables(final Iterable<Runnable> runnables) {
    return transform(
        runnables,
        new Function<Runnable, Callable<Boolean>>() {
          @Override
          public Callable<Boolean> apply(final Runnable input) {
            return new Callable<Boolean>() {
              @Override
              public Boolean call() throws Exception {
                input.run();
                return true;
              }
            };
          }
        }
    );
  }
}