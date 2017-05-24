package com.github.dakusui.actionunit.compat.connectors;

import com.github.dakusui.actionunit.compat.Context;
import com.github.dakusui.actionunit.helpers.Utils;

/**
 * Executes an operation based on an input value.
 *
 * @param <T> Type of input value.
 */
public interface Sink<T> {
  void apply(T input, Context context);

  abstract class Base<T> implements Sink<T> {
    private final String description;

    protected Base(String description) {
      this.description = description;
    }

    protected Base() {
      this(null);
    }

    public void apply(T input, Context context) {
      this.apply(input, Connectors.composeContextValues(context));
    }

    /**
     * Applies this sink to {@code input}.
     *
     * @param input An input to apply this object.
     * @param outer Inputs from outer {@code CompatWith} actions.
     */
    abstract protected void apply(T input, Object... outer);

    @Override
    public String toString() {
      return Utils.nonameIfNull(this.description);
    }
  }
}
