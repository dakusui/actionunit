package com.github.dakusui.actionunit.visitors;

import com.github.dakusui.actionunit.actions.*;
import com.github.dakusui.actionunit.exceptions.Abort;
import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.AutocloseableIterator;
import com.github.dakusui.actionunit.Autocloseables;
import com.github.dakusui.actionunit.Context;
import com.github.dakusui.actionunit.connectors.Connectors;
import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.actionunit.connectors.Source;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.google.common.base.Function;

import java.util.*;
import java.util.concurrent.*;

import static com.github.dakusui.actionunit.Utils.*;
import static com.google.common.base.Preconditions.*;
import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Iterables.size;
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
 */
public abstract class ActionRunner extends Action.Visitor.Base implements Action.Visitor, Context {
  private static final int DEFAULT_THREAD_POOL_SIZE = 5;
  private final int threadPoolSize;

  /**
   * Creates an object of this class with {@code DEFAULT_THREAD_POOL_SIZE}.
   *
   * @param threadPoolSize Size of thread pool used to execute concurrent actions.
   * @see ActionRunner#ActionRunner(int)
   */
  public ActionRunner(int threadPoolSize) {
    checkArgument(threadPoolSize > 0, "Thread pool size must be larger than 0 but %s was given.", threadPoolSize);
    this.threadPoolSize = threadPoolSize;
  }

  /**
   * Creates an instance of this class.
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

  @Override
  public void visit(Named action) {
    action.getAction().accept(this);
  }

  /**
   * {@inheritDoc}
   *
   * @param action
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
   *
   * @param action
   */
  @Override
  public void visit(Concurrent action) {
    final ExecutorService pool = newFixedThreadPool(min(this.threadPoolSize, max(1, size(action))));
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
   *
   * @param action
   */
  @Override
  public void visit(ForEach action) {
    action.getElements().accept(this);
  }

  /**
   * {@inheritDoc}
   *
   * @param action
   */
  @Override
  public void visit(While action) {
    //noinspection unchecked
    while (action.apply(getContextValue())) {
      action.getAction().accept(this);
    }
  }


  /**
   * {@inheritDoc}
   *
   * @param action
   */
  @Override
  public void visit(final With action) {
    action.getAction().accept(createChildFor(action));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(When action) {
    //noinspection unchecked
    if (action.apply(getContextValue())) {
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
        toTask(action.action).run();
      }
      throw ActionException.wrap(lastException);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(final TimeOut action) {
    runWithTimeout(new Callable<Object>() {
                     @Override
                     public Object call() throws Exception {
                       action.action.accept(ActionRunner.this);
                       return true;
                     }
                   },
        action.durationInNanos,
        NANOSECONDS
    );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(final Attempt action) {
    try {
      action.attempt.accept(this);
    } catch (Throwable e) {
      //noinspection unchecked
      if (action.exceptionClass == null || !action.exceptionClass.isAssignableFrom(e.getClass())) {
        throw propagate(e);
      }
      //noinspection unchecked
      new WithResult.IgnoredInPathCalculation.With<>(Connectors.toSource(e), action.recover, action.sinks).accept(this);
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
  protected ActionRunner createChildFor(final With action) {
    return new ActionRunner() {
      @Override
      public ActionRunner getParent() {
        return ActionRunner.this;
      }

      @Override
      public boolean hasValue() {
        return true;
      }

      @Override
      public <T> T value() {
        //noinspection unchecked
        return (T) action.getSource().apply(ActionRunner.this);
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

  private static void acceptTagAction(Tag tagAction, With withAction, ActionRunner runner) {
    tagAction.toLeaf(withAction.getSource(), withAction.getSinks(), runner).accept(runner);
  }

  private Object getContextValue() {
    return this.hasValue() ? this.value() : null;
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

  private Runnable toRunnable(final Action action) {
    return new Runnable() {
      @Override
      public void run() {
        action.accept(ActionRunner.this);
      }
    };
  }

  Iterable<Callable<Boolean>> toCallables(final Iterable<Runnable> runnables) {
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
   * This interface is used to suppress path calculation, which is
   * performed by {@link WithResult}
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
     * A sequential action created by and run as a part of {@code ForEach} action.
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
     * A "with" action created by and run as a part of {@code ForEach} action.
     *
     * @param <U> Type of the value with which child {@code Action} is executed.
     */
    class With<U> extends com.github.dakusui.actionunit.actions.With.Base<U> implements IgnoredInPathCalculation {
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
     * Returns {@code null} since this action runner is a top level one and
     * doesn't have any parent.
     * Subclasses of this class may override this method to return a meaningful
     * object.
     */
    @Override
    public Context getParent() {
      return null;
    }

    @Override
    public boolean hasValue() {
      return false;
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

    public static class Path extends LinkedList<Action> {
      public Path snapshot() {
        Path ret = new Path();
        ret.addAll(this);
        return ret;
      }

      public Path enter(Action action) {
        if (action instanceof IgnoredInPathCalculation) {
          return this;
        }
        action = getAction(action);
        this.add(action);
        return this;
      }

      public Path leave(Action action) {
        if (action instanceof IgnoredInPathCalculation) {
          return this;
        }
        assert getAction(action) == this.remove(this.size() - 1);
        return this;
      }

      private Action getAction(Action action) {
        if (action instanceof Action.Synthesized) {
          action = ((Action.Synthesized) action).getParent();
        }
        return action;
      }
    }

    private final Map<Path, Result> resultMap;

    protected final Path current;


    public WithResult() {
      this(new Path());
    }

    public WithResult(Path current) {
      this(new ConcurrentHashMap<Path, Result>(), current);
    }

    public WithResult(Map<Path, Result> resultMap, Path current) {
      this.resultMap = checkNotNull(resultMap);
      this.current = checkNotNull(current).snapshot();
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
    public void visit(final Leaf action) {
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
    public void visit(final Named action) {
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
    public void visit(final Composite action) {
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
    public void visit(final Sequential action) {
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
    public void visit(final Concurrent action) {
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
    public void visit(final ForEach action) {
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
    public void visit(final While action) {
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
    public void visit(final When action) {
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
    public void visit(final Attempt action) {
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
    public void visit(final With action) {
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
    public void visit(final Retry action) {
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
    public void visit(final TimeOut action) {
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
    protected Iterable<Callable<Boolean>> toCallables(final Concurrent action) {
      return toCallables(toRunnablesWithNewInstance(action));
    }

    private Iterable<Runnable> toRunnablesWithNewInstance(final Iterable<? extends Action> actions) {
      return transform(
          actions,
          new Function<Action, Runnable>() {
            @Override
            public Runnable apply(final Action action) {
              return new Runnable() {
                @Override
                public void run() {
                  action.accept(new WithResult(
                      WithResult.this.resultMap,
                      WithResult.this.current.snapshot()
                  ));
                }
              };
            }
          }
      );
    }

    @Override
    protected ActionRunner createChildFor(final With action) {
      return new ActionRunner.WithResult(this.resultMap, this.current) {
        @Override
        public boolean hasValue() {
          return true;
        }

        @Override
        public <T> T value() {
          //noinspection unchecked
          return (T) action.getSource().apply(ActionRunner.WithResult.this);
        }

        @Override
        public void visit(Tag tagAction) {
          acceptTagAction(tagAction, action, this);
        }
      };
    }

    public ActionPrinter createPrinter() {
      return createPrinter(ActionPrinter.Writer.Std.OUT);
    }

    public ActionPrinter createPrinter(ActionPrinter.Writer writer) {
      return new ActionPrinter<ActionPrinter.Writer>(writer) {
        int nestLevel = 0;
        final Path current = new Path();

        @Override
        public String describeAction(Action action) {
          String ret = format(
              "(%s)%s%s",
              getResultCode(action),
              describe(action),
              getErrorMessage(action)
          );
          int runCount = getRunCount(action);
          return runCount < 2
              ? ret
              : format("%s; %s times", ret, runCount);
        }

        @Override
        public void visit(ForEach action) {
          nestLevel++;
          try {
            super.visit(action);
          } finally {
            nestLevel--;
          }
        }

        @Override
        protected void enter(Action action) {
          this.current.enter(action);
          super.enter(action);
        }

        @Override
        protected void leave(Action action) {
          super.leave(action);
          this.current.leave(action);
        }


        private Result.Code getResultCode(Action action) {
          Path path = this.current.snapshot().enter(action);
          if (resultMap.containsKey(path)) {
            return resultMap.get(path).code;
          }
          return Result.Code.NOTRUN;
        }

        private int getRunCount(Action action) {
          Path path = this.current.snapshot().enter(action);
          if (resultMap.containsKey(path)) {
            return resultMap.get(path).count;
          }
          return 0;
        }

        private String getErrorMessage(Action action) {
          Path path = this.current.snapshot().enter(action);
          if (resultMap.containsKey(path)) {
            Throwable throwable = resultMap.get(path).thrown;
            if (throwable != null) {
              return format("(error=%s)", throwable.getMessage());
            }
          }
          return "";
        }

      };
    }

    private void visitAndRecord(Runnable visit, Action action) {
      boolean succeeded = false;
      Throwable thrown = null;
      this.current.enter(action);
      try {
        visit.run();
        succeeded = true;
      } catch (Error | RuntimeException e) {
        thrown = e;
        throw e;
      } finally {
        if (!(action instanceof IgnoredInPathCalculation)) {
          Path path = this.current.snapshot();
          Result current = resultMap.containsKey(path)
              ? resultMap.get(path)
              : Result.FIRST_TIME;
          if (succeeded) {
            resultMap.put(this.current.snapshot(), current.next(Result.Code.PASSED, null));
          } else {
            if (thrown instanceof AssertionError) {
              resultMap.put(this.current.snapshot(), current.next(Result.Code.FAIL, thrown));
            } else {
              resultMap.put(this.current.snapshot(), current.next(Result.Code.ERROR, thrown));
            }
          }
        }
        this.current.leave(action);
      }
    }

    private Object contextValue() {
      if (this.getParent() == null)
        return null;
      return this.value();
    }

    public static class Result {
      private static final Result FIRST_TIME = new Result(0, Code.NOTRUN, null);
      public final int       count;
      public final Code      code;
      public final Throwable thrown;

      private Result(int count, Code code, Throwable thrown) {
        this.count = count;
        this.code = checkNotNull(code);
        this.thrown = thrown;
      }

      public Result next(Code code, Throwable thrown) {
        return new Result(this.count + 1, code, thrown);
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
        FAIL("F");


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