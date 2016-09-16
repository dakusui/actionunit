package com.github.dakusui.actionunit;

public interface Context {
  /**
   * Returns {@code null} if this object is a top-level context.
   */
  Context getParent();

  boolean hasValue();

  <T> T value();
}
