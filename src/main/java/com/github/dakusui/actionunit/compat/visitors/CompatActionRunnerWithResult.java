package com.github.dakusui.actionunit.compat.visitors;

import com.github.dakusui.actionunit.actions.*;
import com.github.dakusui.actionunit.compat.actions.*;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.helpers.Autocloseables;
import com.github.dakusui.actionunit.visitors.ActionPrinter;
import com.github.dakusui.actionunit.visitors.ReportingActionRunner;
import com.github.dakusui.actionunit.visitors.ActionRunner;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static com.github.dakusui.actionunit.helpers.Checks.checkNotNull;
import static java.lang.String.format;

public class CompatActionRunnerWithResult extends ActionRunner.Impl implements Action.Visitor {

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
      // Value returned by this should be equal to the action given by getAction(action)
      // at this point.
      this.remove(this.size() - 1);
      return this;
    }

    private Action getAction(Action action) {
      if (action instanceof Synthesized) {
        action = ((Synthesized) action).getParent();
      }
      return action;
    }
  }

  private final Map<Path, Result> resultMap;

  protected final Path current;


  /**
   * Creates an instance of this class.
   */
  public CompatActionRunnerWithResult() {
    this(new Path());
  }

  protected CompatActionRunnerWithResult(Path current) {
    this(new ConcurrentHashMap<Path, Result>(), current);
  }

  protected CompatActionRunnerWithResult(Map<Path, Result> resultMap, Path current) {
    this.resultMap = checkNotNull(resultMap);
    this.current = checkNotNull(current).snapshot();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(final Action action) {
    visitAndRecord(
        new Runnable() {
          @Override
          public void run() {
            CompatActionRunnerWithResult.super.visit(action);
          }
        },
        action);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(final Leaf action) {
    visitAndRecord(
        new Runnable() {
          @Override
          public void run() {
            CompatActionRunnerWithResult.super.visit(action);
          }
        },
        action);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(final Named action) {
    visitAndRecord(
        new Runnable() {
          @Override
          public void run() {
            CompatActionRunnerWithResult.super.visit(action);
          }
        },
        action);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(final Composite action) {
    visitAndRecord(
        new Runnable() {
          @Override
          public void run() {
            CompatActionRunnerWithResult.super.visit(action);
          }
        },
        action);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(final Sequential sequential) {
    visitAndRecord(
        new Runnable() {
          @Override
          public void run() {
            CompatActionRunnerWithResult.super.visit(sequential);
          }
        },
        sequential);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(final Concurrent concurrent) {
    visitAndRecord(
        new Runnable() {
          @Override
          public void run() {
            CompatActionRunnerWithResult.super.visit(concurrent);
          }
        },
        concurrent);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(final CompatForEach action) {
    visitAndRecord(
        new Runnable() {
          @Override
          public void run() {
            CompatActionRunnerWithResult.super.visit(action);
          }
        },
        action);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(final CompatWhile while$) {
    visitAndRecord(
        new Runnable() {
          @Override
          public void run() {
            CompatActionRunnerWithResult.super.visit(while$);
          }
        },
        while$);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(final CompatWhen when) {
    visitAndRecord(
        new Runnable() {
          @Override
          public void run() {
            CompatActionRunnerWithResult.super.visit(when);
          }
        },
        when);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(final CompatAttempt action) {
    visitAndRecord(
        new Runnable() {
          @Override
          public void run() {
            CompatActionRunnerWithResult.super.visit(action);
          }
        },
        action);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> void visit(final CompatWith<T> action) {
    visitAndRecord(
        new Runnable() {
          @Override
          public void run() {
            CompatActionRunnerWithResult.super.visit(action);
          }
        },
        action);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(final Retry retry) {
    visitAndRecord(
        new Runnable() {
          @Override
          public void run() {
            CompatActionRunnerWithResult.super.visit(retry);
          }
        },
        retry);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(final TimeOut action) {
    visitAndRecord(
        new Runnable() {
          @Override
          public void run() {
            CompatActionRunnerWithResult.super.visit(action);
          }
        },
        action);
  }

  @Override
  protected Iterable<Callable<Boolean>> toCallables(final Concurrent action) {
    return toCallables(toRunnablesWithNewInstance(action));
  }

  @Override
  protected ActionRunner createChildFor(final CompatWith action) {
    return new CompatActionRunnerWithResult(this.resultMap, this.current) {
      @Override
      public Object value() {
        //noinspection unchecked
        return action.getSource().apply(CompatActionRunnerWithResult.this);
      }

      @Override
      public void visit(Tag tagAction) {
        acceptTagAction(tagAction, action, this);
      }
    };
  }

  /**
   * Creates an {@code Impl} which prints execution results of this
   * object.
   * Results are written to standard output.
   *
   * @see ActionPrinter.Impl
   */
  public ActionPrinter.Impl createPrinter() {
    return createPrinter(ReportingActionRunner.Writer.Std.OUT);
  }

  /**
   * Creates an {@code Impl} which prints execution results of this
   * object with a given {@code writer}.
   *
   * @see ActionPrinter.Impl
   */
  public ActionPrinter.Impl createPrinter(ReportingActionRunner.Writer writer) {
    return new ActionPrinter.Impl(writer) {
      int nestLevel = 0;
      final Path current = new Path();

      @Override
      public String describeAction(Action action) {
        String ret = format(
            "(%s)%s",
            getResultCode(action),
            super.describeAction(action)
        );
        int runCount = getRunCount(action);
        return runCount < 2
            ? ret
            : format("%s; %s times", ret, runCount);
      }

      @Override
      public void visit(CompatForEach action) {
        nestLevel++;
        try {
          super.visit(action);
        } finally {
          nestLevel--;
        }
      }

      @Override
      public <T> void visit(ForEach<T> action) {
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
    };
  }

  public Iterable<Runnable> toRunnablesWithNewInstance(final Iterable<? extends Action> actions) {
    return Autocloseables.transform(
        actions,
        new Function<Action, Runnable>() {
          @Override
          public Runnable apply(final Action action) {
            return new Runnable() {
              @Override
              public void run() {
                action.accept(new CompatActionRunnerWithResult(
                    CompatActionRunnerWithResult.this.resultMap,
                    CompatActionRunnerWithResult.this.current.snapshot()
                ));
              }
            };
          }
        }
    );
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

  public static class Result {
    private static final Result FIRST_TIME = new Result(0, Result.Code.NOTRUN, null);
    public final int         count;
    public final Result.Code code;
    public final Throwable   thrown;

    private Result(int count, Result.Code code, Throwable thrown) {
      this.count = count;
      this.code = checkNotNull(code);
      this.thrown = thrown;
    }

    public Result next(Result.Code code, Throwable thrown) {
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
