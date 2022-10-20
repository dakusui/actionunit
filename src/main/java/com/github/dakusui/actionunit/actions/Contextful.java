package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.ActionSupport;
import com.github.dakusui.actionunit.core.Context;

import java.util.Formatter;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.github.dakusui.actionunit.utils.InternalUtils.toStringIfOverriddenOrNoname;
import static com.github.dakusui.pcond.Requires.requireNonNull;

public interface Contextful<T> extends Leaf {
  Function<Context, T> action();

  <V> V value(Context context);

  String internalVariableName();

  /**
   * A name of a variable this action assigns its result to.
   * This method is intended to return a string to display to humans.
   *
   * @return A human-readable name of the variable.
   */
  String variableName();

  class Impl<T> implements Contextful<T> {

    private final Function<Context, T> function;
    private final String               baseName;


    public Impl(String variableName, Function<Context, T> function) {
      this.baseName = requireNonNull(variableName);
      this.function = requireNonNull(function);
    }

    @Override
    public Function<Context, T> action() {
      return this.function;
    }


    @Override
    public <V> V value(Context context) {
      return requireNonNull(context).valueOf(internalVariableName());
    }

    @Override
    public String variableName() {
      return this.baseName;
    }

    @Override
    public String internalVariableName() {
      return this.baseName + ":" + System.identityHashCode(this);
    }

    @Override
    public Runnable runnable(Context context) {
      return () -> context.assignTo(internalVariableName(), function.apply(context));
    }

    @Override
    public void formatTo(Formatter formatter, int i, int i1, int i2) {
      formatter.format("%s:%s", variableName(), toStringIfOverriddenOrNoname(function));
    }
  }
}
