package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.Action;

import static com.github.dakusui.actionunit.Utils.formatDuration;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

public class Retry extends ActionBase {
  /**
   * A constant that represents an instance of this class should be repeated infinitely.
   */
  public static final int INFINITE = -1;
  public final Action action;
  public final int    times;
  public final long   intervalInNanos;

  public Retry(Action action, long intervalInNanos, int times) {
    checkNotNull(action);
    checkArgument(intervalInNanos >= 0);
    checkArgument(times >= 0 || times == INFINITE);
    this.action = action;
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
}
