package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.exceptions.ActionException;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.github.dakusui.actionunit.helpers.Checks.checkArgument;
import static com.github.dakusui.actionunit.helpers.Checks.checkNotNull;
import static com.github.dakusui.actionunit.helpers.InternalUtils.formatDuration;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class Retry extends ActionBase {
  /**
   * A constant that represents an instance of this class should be repeated infinitely.
   */
  public static final int INFINITE = -1;
  public final  Action                     action;
  public final  int                        times;
  public final  long                       intervalInNanos;
  private final Class<? extends Throwable> targetExceptionClass;
  private final Consumer<Throwable>        handler;

  private <T extends Throwable> Retry(Class<T> targetExceptionClass, Action action, long intervalInNanos, int times, Consumer<Throwable> handler) {
    this.targetExceptionClass = targetExceptionClass;
    this.action = checkNotNull(action);
    this.intervalInNanos = intervalInNanos;
    this.times = times;
    this.handler = handler;
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    return format("%s(%sx%dtimes)",
        formatClassName(),
        formatDuration(intervalInNanos),
        this.times
    );
  }

  public Consumer<Throwable> getHandler() {
    return handler;
  }

  public <T extends Throwable> Class<T> getTargetExceptionClass() {
    //noinspection unchecked
    return (Class<T>) this.targetExceptionClass;
  }

  public static Builder builder(Action action) {
    return new Builder(action);
  }

  public static class Builder {
    private Action action;
    private int                        times                = INFINITE;
    private Class<? extends Throwable> targetExceptionClass = ActionException.class;
    private Consumer<Throwable>        handler              = throwable -> {};

    public Builder(Action action) {
      this.action = requireNonNull(action);
    }

    public Builder times(int times) {
      this.times = times;
      return this;
    }

    public Builder on(Class<? extends Throwable> targetExceptionClass) {
      this.targetExceptionClass = Objects.requireNonNull(targetExceptionClass);
      return this;
    }

    public Builder handler(Consumer<Throwable> handler) {
      this.handler = Objects.requireNonNull(handler);
      return this;
    }

    public Retry withIntervalOf(long interval, TimeUnit timeUnit) {
      checkArgument(interval >= 0);
      checkArgument(times >= 0 || times == INFINITE);

      return new Retry(targetExceptionClass, action, requireNonNull(timeUnit).toNanos(interval), times, handler);
    }
  }

}
