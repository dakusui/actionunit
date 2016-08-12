package com.github.dakusui.actionunit;

import com.github.dakusui.actionunit.visitors.Context;

import java.util.LinkedList;
import java.util.List;

/**
 * Executes an operation based on an input value.
 *
 * @param <T> Type of input value.
 */
public interface Block<T> {
  void apply(T input, Context context);

  /**
   * Applies this block to {@code input}.
   *
   * @param input An input to apply this object.
   * @param outer Inputs from outer {@code With} actions.
   */
  void apply(T input, Object... outer);

  String describe();

  abstract class Base<T> implements Block<T> {
    private final String description;

    protected Base(String description) {
      this.description = description;
    }

    protected Base() {
      this(null);
    }

    public void apply(T input, Context context) {
      List<Object> args = new LinkedList<>();
      Context parent = context.getParent();
      while (parent.getParent() != null) {
        args.add(parent.value());
        parent = parent.getParent();
      }
      this.apply(input, args.toArray());
    }

    @Override
    public String describe() {
      return Utils.nonameIfNull(this.description);
    }
  }
}
