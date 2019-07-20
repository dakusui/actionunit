package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;

import java.util.Formatter;
import java.util.concurrent.TimeUnit;

import static com.github.dakusui.actionunit.utils.Checks.checkArgument;
import static com.github.dakusui.actionunit.utils.InternalUtils.formatDuration;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.SECONDS;

public interface TimeOut extends Action<TimeOut> {
  Action perform();

  long durationInNanos();

  @Override
  default void accept(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  default void formatTo(Formatter formatter, int flags, int width, int precision) {
    formatter.format("timeout in %s", formatDuration(durationInNanos()));
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
        public long durationInNanos() {
          return Builder.this.timeUnit.toNanos(Builder.this.duration);
        }
      };
    }
  }

}
