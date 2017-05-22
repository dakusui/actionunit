package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.Action;

import static com.github.dakusui.actionunit.Utils.formatDuration;
import static com.github.dakusui.actionunit.Checks.checkArgument;
import static com.github.dakusui.actionunit.Checks.checkNotNull;
import static java.lang.String.format;

public class Retry extends ActionBase {
  /**
   * A constant that represents an instance of this class should be repeated infinitely.
   */
  public static final int INFINITE = -1;
  public final  Action                     action;
  public final  int                        times;
  public final  long                       intervalInNanos;
  private final Class<? extends Throwable> targetExceptionClass;

  public <T extends Throwable> Retry(Class<T> targetExceptionClass, Action action, long intervalInNanos, int times) {
    checkArgument(intervalInNanos >= 0);
    checkArgument(times >= 0 || times == INFINITE);
    this.targetExceptionClass = targetExceptionClass;
    this.action = checkNotNull(action);
    this.intervalInNanos = intervalInNanos;
    this.times = times;
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

  public <T extends Throwable> Class<T> getTargetExceptionClass() {
    //noinspection unchecked
    return (Class<T>) this.targetExceptionClass;
  }
}
