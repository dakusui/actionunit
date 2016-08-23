package com.github.dakusui.actionunit;

/**
 * An exception on which retries will not be attempted by {@link com.github.dakusui.actionunit.visitors.ActionRunner}
 * even if it is executing {@link com.github.dakusui.actionunit.Action.Retry}.
 */
public class GiveUp extends ActionException {
  public GiveUp(String message, Throwable t) {
    super(message, t);
  }

  public static GiveUp giveUp() {
    throw giveUp((String)null);
  }

  public static GiveUp giveUp(String message) {
    throw giveUp(message, null);
  }

  public static GiveUp giveUp(Throwable cause) {
    throw giveUp(null, cause);
  }

  public static GiveUp giveUp(String message, Throwable cause) {
    throw new GiveUp(message, cause);
  }
}
