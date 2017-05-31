package com.github.dakusui.actionunit.visitors;

public class WrappedException extends RuntimeException {
  WrappedException(Throwable t) {
    super(t);
  }
}
