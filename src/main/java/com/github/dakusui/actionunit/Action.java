package com.github.dakusui.actionunit;

import com.google.common.base.Preconditions;

import java.util.concurrent.TimeUnit;

import static com.github.dakusui.actionunit.Utils.chooseTimeUnit;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.size;
import static java.lang.String.format;

/**
 * Defines abstract level framework of Action execution mechanism of ActionUnit.
 */
public interface Action {
  class Exception extends RuntimeException {
    public Exception(String message) {
      super(message);
    }

    public Exception(Throwable t) {
      super(t);
    }

    public Exception(String message, Throwable t) {
      super(message, t);
    }
  }

  interface Visitor {
    void visit(Action action);

    void visit(Action.Leaf action);

    void visit(Action.Composite action);

    void visit(Action.Sequential action);

    void visit(Action.Concurrent action);

    void visit(Retry action);

    void visit(TimeOut action);

    <T> void visit(RepeatIncrementally<T> action);

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
      TimeUnit timeUnit = chooseTimeUnit(this.intervalInNanos);
      return format("%s(%d[%s]x%dtimes)",
          this.getClass().getSimpleName(),
          timeUnit.convert(this.intervalInNanos, TimeUnit.NANOSECONDS),
          timeUnit,
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
    public final long   time;

    public TimeOut(Action action, long timeoutInNanos) {
      Preconditions.checkArgument(timeoutInNanos > 0);
      this.time = timeoutInNanos;
      this.action = checkNotNull(action);
    }

    @Override
    public void accept(Visitor visitor) {
      visitor.visit(this);
    }

    @Override
    public String describe() {
      TimeUnit timeUnit = chooseTimeUnit(this.time);
      return format(
          "%s (%s[%s])",
          this.getClass().getSimpleName(),
          timeUnit.convert(this.time, TimeUnit.NANOSECONDS),
          timeUnit
      );
    }
  }

}
