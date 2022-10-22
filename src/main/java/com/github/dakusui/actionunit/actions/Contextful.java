package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.context.FormattableConsumer;

import java.util.Formatter;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.dakusui.actionunit.core.ActionSupport.simple;
import static com.github.dakusui.actionunit.utils.InternalUtils.toStringIfOverriddenOrNoname;
import static com.github.dakusui.printables.PrintableFunctionals.*;
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

    public Function<Context, V> valueSource() {
      return this.valueSource;
    }
  }

  abstract class Base<V> implements Contextful<V> {
    private final String variableName;

    private final String internalVariableName;

    private final Function<Context, V> valueSource;

    private final Action action;

    private final Action      end;

    public Base(String variableName, final String internalVariableName, Function<Context, V> valueSource, Action action, Consumer<V> finisher) {
      this.variableName = variableName;
      this.internalVariableName = internalVariableName;
      this.valueSource = valueSource;
      this.action = action;
      this.end = finisher == null ? null : simple(String.format("done:%s", finisher),
          printableConsumer(new FormattableConsumer<Context>() {
            @Override
            public void accept(Context context) {
              V variable = context.valueOf(internalVariableName);
              context.unassign(internalVariableName); // Unassign first. Otherwise, finisher may fail.
              finisher.accept(variable);
            }

            @Override
            public void formatTo(Formatter formatter, int i, int i1, int i2) {
              formatter.format("%s", toStringIfOverriddenOrNoname(finisher));
            }
          }).describe(String.format("cleanUp:%s", variableName)));
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
    public Optional<Action> close() {
      return Optional.ofNullable(end);
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
