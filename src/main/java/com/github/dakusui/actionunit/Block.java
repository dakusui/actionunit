package com.github.dakusui.actionunit;

/**
 * Executes an operation based on an input value.
 *
 * @param <T> Type of input value.
 */
public interface Block<T> {
  /**
   * Applies this block to {@code input}.
   *
   * @param input  An input to apply this object.
   */
  void apply(T input);
}
