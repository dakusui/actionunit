package com.github.dakusui.actionunit.exceptions;

public class ActionTimeOutException extends ActionException {
  public ActionTimeOutException(String message, Throwable t) {
    super(message, t);
  }
}
