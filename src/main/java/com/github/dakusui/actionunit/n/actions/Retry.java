package com.github.dakusui.actionunit.n.actions;

import com.github.dakusui.actionunit.n.core.Action;

import java.util.concurrent.TimeUnit;

import static com.github.dakusui.actionunit.helpers.Checks.requireArgument;
import static com.github.dakusui.actionunit.helpers.InternalUtils.formatDuration;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.SECONDS;

public interface Retry extends Action {

  Action perform();

  int times();

  Class<? extends Throwable> targetExceptionClass();

  long intervalInNanoseconds();

  default void accept(Visitor visitor) {
    visitor.visit(this);
  }

  class Builder extends Action.Builder<Retry> {

    private final Action                     perform;
    private       int                        times                = 2;
    private       Class<? extends Throwable> targetExceptionClass = Exception.class;
    private       int                        interval             = 10;
    private       TimeUnit                   timeUnit             = SECONDS;

    public Builder(Action perform) {
      this.perform = requireNonNull(perform);
    }

    public Builder times(int times) {
      requireArgument(v -> v >= 0, times);
      this.times = times;
      return this;
    }

    public Builder on(Class<? extends Throwable> targetExceptionClass) {
      this.targetExceptionClass = requireNonNull(targetExceptionClass);
      return this;
    }

    public Builder withIntervalOf(int interval, TimeUnit timeUnit) {
      this.interval = requireArgument(v -> v > 0, interval);
      this.timeUnit = requireNonNull(timeUnit);
      return this;
    }

    @Override
    public Retry build() {
      return new Retry() {
        @Override
        public Action perform() {
          return Builder.this.perform;
        }

        @Override
        public int times() {
          return Builder.this.times;
        }

        @Override
        public Class<? extends Throwable> targetExceptionClass() {
          return Builder.this.targetExceptionClass;
        }

        @Override
        public long intervalInNanoseconds() {
          return timeUnit.toNanos(Builder.this.interval);
        }

        @Override
        public String toString() {
          return String.format("retry(%sx%dtimes)",
              formatDuration(intervalInNanoseconds()),
              times
          );
        }
      };
    }
  }
}
