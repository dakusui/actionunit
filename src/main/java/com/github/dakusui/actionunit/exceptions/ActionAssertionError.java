package com.github.dakusui.actionunit.exceptions;

public class ActionAssertionError extends AssertionError {
  public ActionAssertionError(String message) {
    super(message);
  }
}
