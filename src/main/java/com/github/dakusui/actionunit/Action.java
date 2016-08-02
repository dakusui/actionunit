package com.github.dakusui.actionunit;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import java.util.concurrent.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Math.min;
import static java.lang.String.format;
import static java.util.concurrent.Executors.newFixedThreadPool;

/**
 * Defines abstract level framework of Action execution mechanism of ActionUnit.
 */
public interface Action {
  class Exception extends RuntimeException {
  }

  interface Visitor {
    void visit(Action action);

    void visit(Action.Leaf action);

    void visit(Action.Composite action);

    void visit(Action.Composite.Sequential action);

    void visit(Action.Composite.Concurrent action);

    void visit(Retried action);

    <T> void visit(RepeatedIncrementally<T> action);

    class Impl implements Visitor {
      private static final int THREAD_POOL_SIZE = 5;

      @Override
      public void visit(Action action) {
        throw new UnsupportedOperationException();
      }

      @Override
      public void visit(Leaf action) {
        action.perform();
      }

      @Override
      public void visit(Composite action) {
        throw new UnsupportedOperationException();
      }

      @Override
      public void visit(Sequential action) {
        for (Action each : action.actions) {
          toTask(each).run();
        }
      }

      @Override
      public void visit(Concurrent action) {
        final ExecutorService pool = newFixedThreadPool(min(THREAD_POOL_SIZE, size(action.actions)));
        try {
          for (final Future<Boolean> future : pool.invokeAll(newArrayList(toTasks(action.actions)))) {
            future.get();
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
          pool.shutdown();
        }
      }

      @Override
      public void visit(Retried action) {
        try {
          toTask(action.target).run();
        } catch (Action.Exception e) {
          for (int i = 0; i < action.times; i++) {
            try {
              action.timeUnit.sleep(action.interval);
            } catch (InterruptedException ee) {
              throw propagate(ee);
            }
            toTask(action.target).run();
          }
        }
      }

      @Override
      public <T> void visit(RepeatedIncrementally<T> action) {
        for (T each : action.dataSource) {
          toTask(action.consumerFactory.create(each)).run();
        }
      }

      private Iterable<Callable<Boolean>> toTasks(final Iterable<? extends Action> actions) {
        return Iterables.transform(
            actions,
            new Function<Action, Callable<Boolean>>() {
              @Override
              public Callable<Boolean> apply(final Action input) {
                return new Callable<Boolean>() {
                  @Override
                  public Boolean call() throws Exception {
                    toTask(input).run();
                    return true;
                  }
                };
              }
            }
        );
      }

      /**
       * An extension point to allow users to customize how an action will be
       * executed by this {@code Visitor}.
       *
       * @param action An action executed by
       */
      @SuppressWarnings("WeakerAccess")
      protected Runnable toTask(final Action action) {
        return new Runnable() {
          @Override
          public void run() {
            action.accept(Impl.this);
          }
        };
      }
    }
  }

  void accept(Visitor visitor);

  String describe();

  /**
   * A base class of all {@code Action}s.
   */
  abstract class Base implements Action {
  }

  /**
   * Any action that actually does any meaningful operation outside "ActionUnit"
   * framework should extend this class.
   */
  abstract class Leaf extends Base {
    @Override
    public void accept(Visitor visitor) {
      visitor.visit(this);
    }

    abstract public void perform();
  }

  class Retried extends Base {
    public final Action   target;
    public final int      times;
    public final int      interval;
    public final TimeUnit timeUnit;

    public Retried(Action target, int interval, TimeUnit timeUnit, int times) {
      checkNotNull(target);
      checkArgument(interval >= 0);
      checkNotNull(timeUnit);
      checkArgument(times >= 0);
      this.target = target;
      this.interval = interval;
      this.timeUnit = timeUnit;
      this.times = times;
    }

    @Override
    public void accept(Visitor visitor) {
      visitor.visit(this);
    }

    @Override
    public String describe() {
      return format("%s(%d[%s]x%dtimes)",
          this.getClass().getSimpleName(),
          this.interval,
          this.timeUnit,
          this.times
      );
    }
  }

  class RepeatedIncrementally<T> extends Base {
    final         Iterable<T>        dataSource;
    private final ConsumerFactory<T> consumerFactory;

    public RepeatedIncrementally(Iterable<T> dataSource, ConsumerFactory<T> consumerFactory) {
      this.dataSource = checkNotNull(dataSource);
      this.consumerFactory = checkNotNull(consumerFactory);
    }

    @Override
    public void accept(Visitor visitor) {
      visitor.visit(this);
    }

    @Override
    public String describe() {
      return format(
          "%s (%d items, %s)",
          this.getClass().getSimpleName(),
          size(this.dataSource),
          this.consumerFactory.describe()
      );
    }

    interface ConsumerFactory<T> {
      Action create(T target);

      String describe();
    }
  }

  abstract class Composite extends Base {
    public final  Iterable<? extends Action> actions;
    private final String                     summary;

    public Composite(String summary, Iterable<? extends Action> actions) {
      this.summary = summary;
      this.actions = checkNotNull(actions);
    }

    public String describe() {
      return this.summary == null
          ? format("%d actions", size(actions))
          : format("%s (%s actions)", this.summary, size(actions));
    }
  }

  /**
   * A class that represents a set of actions that should be executed sequentially.
   */
  class Concurrent extends Composite {
    public Concurrent(String summary, Iterable<? extends Action> actions) {
      super(summary, actions);
    }

    @Override
    public void accept(Visitor visitor) {
      visitor.visit(this);
    }

    public enum Factory {
      INSTANCE;

      public Action.Concurrent create(String summary, Iterable<? extends Action> actions) {
        return new Action.Concurrent(summary, actions);
      }
    }
  }

  /**
   * A class that represents a sequence of actions that should be executed one
   * after another.
   */
  class Sequential extends Composite {
    public Sequential(String summary, Iterable<? extends Action> actions) {
      super(summary, actions);
    }

    @Override
    public void accept(Visitor visitor) {
      visitor.visit(this);
    }

    public enum Factory {
      INSTANCE;

      public Action.Sequential create(String summary, Iterable<? extends Action> actions) {
        return new Action.Sequential(summary, actions);
      }
    }
  }
}
