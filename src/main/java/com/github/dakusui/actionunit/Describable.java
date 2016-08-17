package com.github.dakusui.actionunit;

public interface Describable {
  /**
   * Describes this object.
   * This method should give a more descriptive and useful string for ActionUnit's
   * use cases than {@code toString()} method.
   *
   * @see Describables#describe(Object)
   */
  String describe();
}
