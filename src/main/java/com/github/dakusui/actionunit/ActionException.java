package com.github.dakusui.actionunit;

public class ActionException extends RuntimeException {
  public ActionException(String message) {
    this(message, null);
  }

  public ActionException(Throwable t) {
    this(null, t);
  }

  public ActionException(String message, Throwable t) {
    super(message, t);
  }
}
