package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.Action;
import com.google.common.base.Preconditions;

import static com.github.dakusui.actionunit.Utils.formatDuration;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * Created by hiroshi on 9/1/16.
 */
public class TimeOut extends ActionBase {
  /**
   * A constant which means an instance of this class should wait forever.
   */
  public static final int FOREVER = -1;
  public final Action action;
  public final long   durationInNanos;

  /**
   * Creates an object of this class.
   *
   * @param action         Action to be monitored and interrupted by this object.
   * @param timeoutInNanos Duration to time out in nano seconds.
   */
  public TimeOut(Action action, long timeoutInNanos) {
    Preconditions.checkArgument(timeoutInNanos > 0 || timeoutInNanos == FOREVER,
        "Timeout duration must be positive or %d (FOREVER) but %d was given",
        FOREVER,
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
