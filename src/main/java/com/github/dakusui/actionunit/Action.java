package com.github.dakusui.actionunit;

import com.google.common.collect.Iterables;

import static com.google.common.base.Preconditions.checkNotNull;

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
        throw new UnsupportedOperationException();
      }
    }

    interface Provider {
      class ForDefaultRunner implements Provider {
        @Override
        public Visitor create() {
          return new Impl();
        }
      }
      Visitor create();
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

  abstract class Targeted<T> extends Leaf {
    private final T      target;

    protected Targeted(T target) {
      this.target = checkNotNull(target);
    }

    public void perform() {
      perform(this.target);
    }

    protected abstract void perform(T target);
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
          ? String.format("%d actions", Iterables.size(actions))
          : String.format("%s (%s actions)", this.summary, Iterables.size(actions));
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
