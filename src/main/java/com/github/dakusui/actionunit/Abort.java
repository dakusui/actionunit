package com.github.dakusui.actionunit;

/**
 * An exception on which retries will not be attempted by {@link com.github.dakusui.actionunit.visitors.ActionRunner}
 * even if it is executing {@link com.github.dakusui.actionunit.Action.Retry}.
 */
public class Abort extends ActionException {
  public Abort(String message, Throwable t) {
    super(message, t);
  }

  public static Abort abort() {
    throw abort((String)null);
  }

  public static Abort abort(String message) {
    throw abort(message, null);
  }

  public static Abort abort(Throwable cause) {
    throw abort(null, cause);
  }

  public static Abort abort(String message, Throwable cause) {
    throw new Abort(message, cause);
  }
}
