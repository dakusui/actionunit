package com.github.dakusui.actionunit.visitors;

public interface Context {
  /**
   * Returns {@code null} if this object is a top-level context.
   */
  Context getParent();
  <T> T value();
}
