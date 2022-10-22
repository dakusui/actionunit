package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.printables.PrintableFunctionals;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.dakusui.actionunit.utils.InternalUtils.toStringIfOverriddenOrNoname;
import static com.github.dakusui.printables.PrintableFunctionals.*;
import static java.util.Objects.requireNonNull;

public interface Contextful<V> extends Action {
  /**
   * Returns a main action.
   *
   * @return A main action.
   */
  Action action();

  /**
   * Returns a "close" action which takes care of "clean up"
   *
   * @return A "close" action.
   */
  Optional<Action> close();

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
    private final String internalVariableName;
    private final String variableName;

    private Action action;

    protected Builder(String variableName) {
      this.variableName = requireNonNull(variableName);
      this.internalVariableName = variableName + ":" + System.identityHashCode(this);

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

    public <W> Function<Context, W> function(Function<V, W> function) {
      return printableFunction((Context context) -> function.apply(context.valueOf(internalVariableName())))
          .describe(toStringIfOverriddenOrNoname(function));

    }

    public Consumer<Context> consumer(Consumer<V> consumer) {
      return printableConsumer((Context context) -> consumer.accept(context.valueOf(internalVariableName())))
          .describe(toStringIfOverriddenOrNoname(consumer));
    }

    public Predicate<Context> predicate(Predicate<V> predicate) {
      return printablePredicate(
          (Context context) -> predicate.test(context.valueOf(internalVariableName())))
          .describe(() -> variableName() + ":" + toStringIfOverriddenOrNoname(predicate));
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
  }
}
