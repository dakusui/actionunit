package com.github.dakusui.actionunit;

import com.google.common.base.Preconditions;

import static com.github.dakusui.actionunit.Utils.formatDurationInNanos;
import static com.github.dakusui.actionunit.Utils.nonameIfNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.size;
import static java.lang.String.format;

/**
 * Defines abstract level framework of Action execution mechanism of ActionUnit.
 */
public interface Action {

  interface Visitor {
    void visit(Action action);

    void visit(Action.Leaf action);

    void visit(Action.Composite action);

    void visit(Action.Sequential action);

    void visit(Action.Concurrent action);

    void visit(Retry action);

    void visit(TimeOut action);

    <T> void visit(RepeatIncrementally<T> action);

    abstract class Base implements Visitor {
      @Override
      public void visit(Leaf action) {
        this.visit((Action)action);
      }

      @Override
      public void visit(Composite action) {
        this.visit((Action)action);
      }

      @Override
      public void visit(Sequential action) {
        this.visit((Action.Composite)action);
      }

      @Override
      public void visit(Concurrent action) {
        this.visit((Action.Composite)action);
      }

      @Override
      public void visit(Retry action) {
        this.visit((Action)action);
      }

      @Override
      public void visit(TimeOut action) {
        this.visit((Action)action);
      }

      @Override
      public <T> void visit(RepeatIncrementally<T> action) {
        this.visit((Action)action);
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

  abstract class WithTarget<T> extends Leaf {
    final T target;

    protected WithTarget(T target) {
      this.target = target;
    }

    @Override
    public void perform() {
      this.perform(this.target);
    }

    protected abstract void perform(T target);

    public interface Factory<T> {
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
      return format("%s (%s, %s actions)", nonameIfNull(this.summary), this.getClass().getSimpleName(), size(actions));
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

  class Retry extends Base {
    public final Action action;
    public final int    times;
    public final long   intervalInNanos;

    public Retry(Action action, long intervalInNanos, int times) {
      checkNotNull(action);
      checkArgument(intervalInNanos >= 0);
      checkArgument(times >= 0);
      this.action = action;
      this.intervalInNanos = intervalInNanos;
      this.times = times;
    }

    @Override
    public void accept(Visitor visitor) {
      visitor.visit(this);
    }

    @Override
    public String describe() {
      return format("%s(%sx%dtimes)",
          this.getClass().getSimpleName(),
          formatDurationInNanos(intervalInNanos),
          this.times
      );
    }
  }

  class RepeatIncrementally<T> extends Base {
    public final Iterable<T>                  dataSource;
    public final Action.WithTarget.Factory<T> factoryForActionWithTarget;

    public RepeatIncrementally(Iterable<T> dataSource, WithTarget.Factory<T> factoryForActionWithTarget) {
      this.dataSource = checkNotNull(dataSource);
      this.factoryForActionWithTarget = checkNotNull(factoryForActionWithTarget);
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
          this.factoryForActionWithTarget.describe()
      );
    }
  }

  class TimeOut extends Base {
    public final Action action;
    public final long   durationInNanos;

    public TimeOut(Action action, long timeoutInNanos) {
      Preconditions.checkArgument(timeoutInNanos > 0);
      this.durationInNanos = timeoutInNanos;
      this.action = checkNotNull(action);
    }

    @Override
    public void accept(Visitor visitor) {
      visitor.visit(this);
    }

    @Override
    public String describe() {
      return format(
          "%s (%s)",
          this.getClass().getSimpleName(),
          formatDurationInNanos(this.durationInNanos)
      );
    }
  }

}
