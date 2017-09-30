package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.exceptions.ActionException;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.github.dakusui.actionunit.helpers.Checks.checkArgument;
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

  private Retry(int id, Class<? extends Throwable> targetExceptionClass, Action action, long intervalInNanos, int times, Consumer<Throwable> handler) {
    super(id);
    this.targetExceptionClass = targetExceptionClass;
    this.action = action;
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

  public static Builder builder(int id, Action action) {
    return new Builder(id, action);
  }

  public static class Builder {
    private final int    id;
    private       Action action;
    private int                        times                = INFINITE;
    private Class<? extends Throwable> targetExceptionClass = ActionException.class;
    private TimeUnit                   timeUnit             = null;
    private long                       interval             = -1;
    private Consumer<Throwable>        handler              = throwable -> {};

    public Builder(int id, Action action) {
      this.id = id;
      this.action = requireNonNull(action);
    }

    public Builder times(int times) {
      checkArgument(times >= 0 || times == INFINITE);
      this.times = times;
      return this;
    }

    public Builder on(Class<? extends Throwable> targetExceptionClass) {
      this.targetExceptionClass = requireNonNull(targetExceptionClass);
      return this;
    }

    public Builder handler(Consumer<Throwable> handler) {
      this.handler = requireNonNull(handler);
      return this;
    }

    public Builder withIntervalOf(long interval, TimeUnit timeUnit) {
      checkArgument(interval >= 0);
      this.interval = interval;
      this.timeUnit = requireNonNull(timeUnit);
      return this;
    }

    public Retry build() {
      return new Retry(id, targetExceptionClass, action, timeUnit.toNanos(interval), times, handler);
    }
  }
}
