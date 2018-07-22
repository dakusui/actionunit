package com.github.dakusui.actionunit.core.generator;

public interface StringGenerator<I> extends ValueGenerator<I, String> {
  /**
   * {@code value} must be assigned at {@code assemble} time.
   *
   * @param value
   * @param <I>
   * @return
   */
  static <I> StringGenerator<I> of(String value) {
    return v -> c -> value;
  }
}
