package com.github.dakusui.actionunit.compat;

public interface Context {
  /**
   * Returns {@code null} if this object is a top-level context.
   */
  Context getParent();

  Object value();
}