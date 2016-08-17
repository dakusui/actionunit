package com.github.dakusui.actionunit.visitors;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.ActionException;
import com.github.dakusui.actionunit.Context;
import com.google.common.base.Function;

import java.util.Map;
import java.util.concurrent.*;

import static com.github.dakusui.actionunit.Describables.describe;
import static com.github.dakusui.actionunit.Utils.runWithTimeout;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
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
 * action.accept(new ActionRunner.Impl());
 * </code>
 */
public abstract class ActionRunner extends Action.Visitor.Base implements Action.Visitor, Context {
  public static final int DEFAULT_THREAD_POOL_SIZE = 5;
  private final int threadPoolSize;

  /**
   * Creates an object of this class.
   *
   * @param threadPoolSize Size of thread pool used to execute concurrent actions.
   */
  public ActionRunner(int threadPoolSize) {
    checkArgument(threadPoolSize > 0, "Thread pool size must be larger than 0 but %s was given.", threadPoolSize);
    this.threadPoolSize = threadPoolSize;
  }

  /**
   * Creates an object of this class with {@code DEFAULT_THREAD_POOL_SIZE}.
   *
   * @see ActionRunner#ActionRunner(int)
   */
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
   *
   * @param action
   */
  @Override
  public void visit(Action.Sequential action) {
    for (Action each : action) {
      toRunnable(each).run();
    }
  }

  /**
   * {@inheritDoc}
   *
   * @param action
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

  /**
   * {@inheritDoc}
   *
   * @param action
   */
  @Override
  public void visit(Action.ForEach action) {
    action.getElements().accept(this);
  }

  /**
   * {@inheritDoc}
   *
   * @param action
   */
  @Override
  public void visit(final Action.With action) {
    action.getAction().accept(createChildFor(action));
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
  protected ActionRunner createChildFor(final Action.With action) {
    return new ActionRunner() {
      @Override
      public ActionRunner getParent() {
        return ActionRunner.this;
      }

      @Override
      public <T> T value() {
        //noinspection unchecked
        return (T) action.source().apply(ActionRunner.this);
      }

      @Override
      public void visit(Action.With.Tag tagAction) {
        acceptTagAction(tagAction, action, this);
      }
    };
  }

  protected static void acceptTagAction(Action.With.Tag tagAction, Action.With withAction, ActionRunner runner) {
    tagAction.toLeaf(withAction.source(), withAction.getSinks(), runner).accept(runner);
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

  /**
   * A simple implementation of an {@link ActionRunner}.
   */
  public static class Impl extends ActionRunner {
    /**
     * Creates an object of this class.
     */
    public Impl() {
    }

    /**
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
    public <T> T value() {
      //noinspection unchecked
      throw new UnsupportedOperationException();
    }
  }

  public static class WithResult extends ActionRunner.Impl implements Action.Visitor {
    private final Map<Action, Result> resultMap;

    public WithResult() {
      this.resultMap = new ConcurrentHashMap<>();
    }

    @Override
    public void visit(final Action action) {
      visitAndRecord(
          new Runnable() {
            @Override
            public void run() {
              WithResult.super.visit(action);
            }
          },
          action);
    }

    @Override
    public void visit(final Action.Leaf action) {
      visitAndRecord(
          new Runnable() {
            @Override
            public void run() {
              WithResult.super.visit(action);
            }
          },
          action);
    }

    @Override
    public void visit(final Action.Composite action) {
      visitAndRecord(
          new Runnable() {
            @Override
            public void run() {
              WithResult.super.visit(action);
            }
          },
          action);
    }

    @Override
    public void visit(final Action.Sequential action) {
      visitAndRecord(
          new Runnable() {
            @Override
            public void run() {
              WithResult.super.visit(action);
            }
          },
          action);
    }

    @Override
    public void visit(final Action.Concurrent action) {
      visitAndRecord(
          new Runnable() {
            @Override
            public void run() {
              WithResult.super.visit(action);
            }
          },
          action);
    }

    @Override
    public void visit(final Action.ForEach action) {
      visitAndRecord(
          new Runnable() {
            @Override
            public void run() {
              WithResult.super.visit(action);
            }
          },
          action);
    }

    @Override
    public void visit(final Action.With.Tag action) {
      visitAndRecord(
          new Runnable() {
            @Override
            public void run() {
              WithResult.super.visit(action);
            }
          },
          action);
    }

    @Override
    public void visit(final Action.With action) {
      visitAndRecord(
          new Runnable() {
            @Override
            public void run() {
              WithResult.super.visit(action);
            }
          },
          action);
    }

    @Override
    public void visit(final Action.Retry action) {
      visitAndRecord(
          new Runnable() {
            @Override
            public void run() {
              WithResult.super.visit(action);
            }
          },
          action);
    }

    @Override
    public void visit(Action.TimeOut action) {
      super.visit(action);
    }

    public ActionPrinter createPrinter() {
      return createPrinter(ActionPrinter.Writer.Std.OUT);
    }

    public ActionPrinter createPrinter(ActionPrinter.Writer writer) {
      return new ActionPrinter<ActionPrinter.Writer>(writer) {
        int forEachLevel = 0;

        @Override
        public String describeAction(Action action) {
          return String.format(
              "(%s)%s",
              getResultCode(action),
              describe(action)
          );
        }

        @Override
        public void visit(Action.ForEach action) {
          forEachLevel++;
          try {
            super.visit(action);
          } finally {
            forEachLevel--;
          }
        }

        private Result.Code getResultCode(Action action) {
          if (forEachLevel == 0 || (forEachLevel == 1 && Action.ForEach.class.isAssignableFrom(action.getClass()))) {
            if (resultMap.containsKey(action)) {
              return resultMap.get(action).code;
            }
            return Result.Code.NOTRUN;
          } else {
            return Result.Code.NA;
          }
        }
      };
    }

    private void visitAndRecord(Runnable visit, Action action) {
      boolean succeeded = false;
      Throwable thrown = null;
      try {
        visit.run();
        succeeded = true;
      } catch (Error | RuntimeException e) {
        thrown = e;
        throw e;
      } finally {
        if (succeeded) {
          resultMap.put(action, new Result(Result.Code.PASSED, contextValue(), null));
        } else {
          if (thrown instanceof AssertionError) {
            resultMap.put(action, new Result(Result.Code.FAIL, contextValue(), thrown));
          } else {
            resultMap.put(action, new Result(Result.Code.ERROR, contextValue(), thrown));
          }
        }
      }
    }

    private Object contextValue() {
      if (this.getParent() == null)
        return null;
      return this.value();
    }

    public static class Result {
      public final Code      code;
      public final Object    contextValue;
      public final Throwable thrown;

      protected Result(Code code, Object contextValue, Throwable thrown) {
        this.code = checkNotNull(code);
        this.contextValue = contextValue;
        this.thrown = thrown;
      }

      public enum Code {
        /**
         * Not run yet
         */
        NOTRUN(" "),
        /**
         * Action was performed successfully.
         */
        PASSED("+"),
        /**
         * An exception but {@link AssertionError} was thrown.
         */
        ERROR("E"),
        /**
         * Mismatched expectation.
         */
        FAIL("F"),
        /**
         * Action is not applicable. This code is used for actions under
         * {@link Action.ForEach}, which are instantiated
         * every time for each value it gives.
         */
        NA("-");

        private final String symbol;

        Code(String symbol) {
          this.symbol = symbol;
        }

        public String toString() {
          return this.symbol;
        }
      }
    }
  }
}