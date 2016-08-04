package com.github.dakusui.actionunit;

public class ActionException extends RuntimeException {
  public ActionException(String message) {
    super(message);
  }

  public ActionException(Throwable t) {
    super(t);
  }

  public ActionException(String message, Throwable t) {
    super(message, t);
  }
}
