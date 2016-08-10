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
   * @param input An input to apply this object.
   */
  void apply(T input);

  String describe();

  abstract class Base<T> implements Block<T> {
    private final String description;

    protected Base(String description) {
      this.description = description;
    }

    protected Base() {
      this(null);
    }

    @Override
    public String describe() {
      return Utils.nonameIfNull(this.description);
    }
  }
}
