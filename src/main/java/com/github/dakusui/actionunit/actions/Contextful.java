package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public interface Contextful<V> extends Action {
  /**
   * A function to provide a value referenced from inside an action returned by the
   * {@link Contextful#action()} method.
   *
   * @return A function to provide a value for an action.
   */
  Function<Context, V> valueSource();

  /**
   * Returns a main action.
   *
   * @return A main action.
   */
  Action action();

  /**
   * A name of the variable.
   * The string returned by this method is used only for printing an action tree.
   * To identify a variable, a string returned by {@link Contextful#internalVariableName()}.
   *
   * @return A human-readable variable name.
   */
  String variableName();

  /**
   * @return An internal context variable name.
   */
  String internalVariableName();


  abstract class Builder<B extends Builder<B, A, V>, A extends Contextful<V>, V> extends Action.Builder<A> {
    private final Function<Context, V> valueSource;
    private final String internalVariableName;
    private final String variableName;

    private Action action;

    protected Builder(String variableName, Function<Context, V> function) {
      this.variableName = requireNonNull(variableName);
      this.internalVariableName = variableName + ":" + System.identityHashCode(this);
      this.valueSource = requireNonNull(function);
    }

    @SuppressWarnings("unchecked")
    public B action(Action action) {
      this.action = requireNonNull(action);
      return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B action(Function<B, Action> action) {
      return this.action(action.apply((B) this));
    }


    public Action action() {
      return this.action;
    }

    public String variableName() {
      return this.variableName;
    }

    public String internalVariableName() {
      return this.internalVariableName;
    }

    protected static String nextVariableName(String variableName) {
      requireNonNull(variableName);
      if (variableName.length() == 1 && 'a' <= variableName.charAt(0) && variableName.charAt(0) <= 'z')
        return Character.toString((char) (variableName.charAt(0) + 1));
      if (variableName.matches(".*_[1-9][0-9]*$")) {
        int index = Integer.parseInt(variableName.replaceAll(".*_", "")) + 1;
        return variableName.replaceAll("_[1-9][0-9]*$", "_" + index);
      }
      return variableName + "_1";
    }

    public Function<Context, V> valueSource() {
      return this.valueSource;
    }
  }

  abstract class Base<V> implements Contextful<V> {
    private final String variableName;

    private final String internalVariableName;

    private final Function<Context, V> valueSource;

    private final Action action;


    public Base(String variableName, final String internalVariableName, Function<Context, V> valueSource, Action action) {
      this.variableName = variableName;
      this.internalVariableName = internalVariableName;
      this.valueSource = valueSource;
      this.action = action;
    }

    @Override
    public Function<Context, V> valueSource() {
      return valueSource;
    }

    @Override
    public Action action() {
      return action;
    }

    @Override
    public String variableName() {
      return variableName;
    }

    @Override
    public String internalVariableName() {
      return internalVariableName;
    }

  }
}
