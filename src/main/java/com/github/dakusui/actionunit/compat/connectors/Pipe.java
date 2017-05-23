package com.github.dakusui.actionunit.compat.connectors;

import com.github.dakusui.actionunit.Context;
import com.github.dakusui.actionunit.Utils;

/**
 * Executes an operation based on an input value and gives an output value.
 *
 * @param <I> Type of input value
 * @param <O> Type of output value
 */
public interface Pipe<I, O> {
  O apply(I input, Context context);

  abstract class Base<I, O> implements Pipe<I, O> {
    private final String description;

    protected Base(String description) {
      this.description = description;
    }

    protected Base() {
      this(null);
    }

    @Override
    public O apply(I input, Context context) {
      return this.apply(input, Connectors.composeContextValues(context));
    }

    /**
     * Applies this pipe to {@code input}.
     *
     * @param input An input to apply this object.
     * @param outer Inputs from outer {@code CompatWith} actions.
     */
    abstract protected O apply(I input, Object... outer);

    @Override
    public String toString() {
      return this.description == null
          ? Utils.shortClassNameOf(this.getClass())
          : description;
    }
  }
}
