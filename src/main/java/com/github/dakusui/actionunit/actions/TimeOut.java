package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.Action;

import static com.github.dakusui.actionunit.Checks.checkArgument;
import static com.github.dakusui.actionunit.Checks.checkNotNull;
import static com.github.dakusui.actionunit.Utils.formatDuration;
import static java.lang.String.format;

public class TimeOut extends ActionBase {
  public final Action action;
  public final long   durationInNanos;

  /**
   * Creates an object of this class.
   *
   * @param action         Action to be monitored and interrupted by this object.
   * @param timeoutInNanos Duration to time out in nano seconds.
   */
  public TimeOut(Action action, long timeoutInNanos) {
    checkArgument(timeoutInNanos > 0,
        "Timeout duration must be positive  but %d was given",
        timeoutInNanos
    );
    this.durationInNanos = timeoutInNanos;
    this.action = checkNotNull(action);
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    return format(
        "%s(%s)",
        formatClassName(),
        formatDuration(this.durationInNanos)
    );
  }
}
