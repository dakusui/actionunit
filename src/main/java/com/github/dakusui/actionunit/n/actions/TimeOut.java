package com.github.dakusui.actionunit.n.actions;

import com.github.dakusui.actionunit.n.core.Action;

import java.util.concurrent.TimeUnit;

import static com.github.dakusui.actionunit.helpers.Checks.checkArgument;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.SECONDS;

public interface TimeOut extends Action {
  Action perform();

  long durationInNnanos();

  default void accept(Visitor visitor) {
    visitor.visit(this);
  }

  class Builder extends Action.Builder<TimeOut> {
    Action action;
    long   duration = 10;
    private TimeUnit timeUnit = SECONDS;

    public Builder(Action action) {
      this.action = requireNonNull(action);
    }

    public Action in(long duration, TimeUnit timeUnit) {
      checkArgument(duration > 0,
          "Timeout duration must be positive  but %d was given",
          duration
      );
      requireNonNull(timeUnit);
      this.duration = duration;
      this.timeUnit = timeUnit;
      return this.$();
    }

    @Override
    public TimeOut build() {
      return new TimeOut() {
        @Override
        public Action perform() {
          return Builder.this.action;
        }

        @Override
        public long durationInNnanos() {
          return Builder.this.timeUnit.toNanos(Builder.this.duration);
        }
      };
    }
  }

}
