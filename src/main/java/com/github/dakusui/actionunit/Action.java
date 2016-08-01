package com.github.dakusui.actionunit;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;

import java.util.concurrent.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Lists.newArrayList;

/**
 * Defines abstract level framework of Action execution mechanism of ActionUnit.
 */
public interface Action {
  interface Visitor {
    void visit(Action action);

    void visit(Action.Leaf action);

    void visit(Action.Composite action);

    void visit(Action.Composite.Sequential action);

    void visit(Action.Composite.Concurrent action);

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
          each.accept(this);
        }
      }

      @Override
      public void visit(Concurrent action) {
        runActionsInThreadPool(action.actions);
      }

      private void runActionsInThreadPool(Iterable<? extends Action> actions) {
        final ExecutorService pool = Executors.newFixedThreadPool(Math.min(THREAD_POOL_SIZE, size(actions)));
        try {
          for (final Future<Boolean> future : pool.invokeAll(newArrayList(toTasks(actions)))) {
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

  String format();

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

  abstract class Retrying extends Base {
    public Retrying(int interval, TimeUnit timeUnit, int times) {

    }
  }

  abstract class Consuming<T> extends Base {
    final Supplier<T> supplier;

    protected Consuming(Supplier<T> supplier) {
      this.supplier = supplier;
    }


    abstract Supplier<T> getSupplier();
  }

  abstract class Composite extends Base {
    public final  Iterable<? extends Action> actions;
    private final String                     summary;

    public Composite(String summary, Iterable<? extends Action> actions) {
      this.summary = summary;
      this.actions = checkNotNull(actions);
    }

    public String format() {
      return this.summary == null
          ? String.format("%d actions", size(actions))
          : String.format("%s (%s actions)", this.summary, size(actions));
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
