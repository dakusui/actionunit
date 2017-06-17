package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.github.dakusui.actionunit.helpers.Checks.checkArgument;
import static com.github.dakusui.actionunit.helpers.Checks.checkNotNull;
import static com.github.dakusui.actionunit.helpers.InternalUtils.formatDuration;
import static java.lang.String.format;

public class TimeOut extends ActionBase {
  public final Action action;
  public final long   durationInNanos;


  public static class Builder {
    private final int id;
    Action action;
    long duration = -1;

    public Builder(int id, Action action) {
      this.id = id;
      this.action = Objects.requireNonNull(action);
    }

    public TimeOut in(long duration, TimeUnit timeUnit) {
      checkArgument(duration > 0,
          "Timeout duration must be positive  but %d was given",
          duration
      );
      this.duration = duration;
      return new TimeOut(
          this.id,
          this.action,
          Objects.requireNonNull(timeUnit).toNanos(this.duration)
      );
    }
  }

  /**
   * Creates an object of this class.
   *
   * @param action         Action to be monitored and interrupted by this object.
   * @param timeoutInNanos Duration to time out in nano seconds.
   */
  private TimeOut(int id, Action action, long timeoutInNanos) {
    super(id);
    // This check is still necessary because the value can overflow.
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
