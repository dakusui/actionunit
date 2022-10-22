package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.printables.PrintableFunctionals;

import java.util.Formatter;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.github.dakusui.actionunit.core.ActionSupport.simple;
import static com.github.dakusui.actionunit.core.context.FormattableConsumer.nopConsumer;
import static com.github.dakusui.actionunit.utils.InternalUtils.toStringIfOverriddenOrNoname;
import static java.util.Objects.requireNonNull;

/**
 * An interface to access a context variable safely.
 * An instance of this interface corresponds to single context variable.
 *
 * @param <V> A type of variable through which this action interacts with another.
 * @see Context
 */
public interface With<V> extends Contextful<V> {
  @Override
  default void accept(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  default void formatTo(Formatter formatter, int flags, int width, int precision) {
    formatter.format("with:" + variableName() + ":" + toStringIfOverriddenOrNoname(valueSource()));
  }

  class Builder<V> extends Contextful.Builder<Builder<V>, With<V>, V> {

    public Builder(String variableName, Function<Context, V> function) {
      super(variableName, function);
    }

    /**
     * Creates an action that updates the context variable.
     * A current value of the context variable is given to the `function` and the result
     * of the function is written back to the context.
     *
     * @param function A function to compute a new value of the context variable.
     * @return An action that updates the context variable.
     */
    public Action updateVariableWith(Function<V, V> function) {
      return simple(
          toStringIfOverriddenOrNoname(function) + ":" + variableName() + "*",
          (Context c) -> variableUpdateFunction(function).apply(c));
    }

    /**
     * Creates an action that consumes the context variable.
     *
     * @param consumer A consumer that processes the context variable.
     * @return A created action.
     */
    public Action createAction(Consumer<V> consumer) {
      return simple(toStringIfOverriddenOrNoname(consumer) + ":" + variableName(),
          (Context c) -> variableReferenceConsumer(consumer).accept(c));
    }

    public <W> Builder<W> nest(Function<V, W> function) {
      return new Builder<>(nextVariableName(variableName()), function(function));
    }


    private Function<Context, V> variableUpdateFunction(Function<V, V> function) {
      return PrintableFunctionals.printableFunction(
              (Context context) -> {
                V ret = function.apply(context.valueOf(internalVariableName()));
                context.assignTo(internalVariableName(), ret);
                return ret;
              })
          .describe("XYZ");
    }

    private Consumer<Context> variableReferenceConsumer(Consumer<V> consumer) {
      return (Context context) -> consumer.accept(context.valueOf(internalVariableName()));

    }


    public With<V> build() {
      return build(nopConsumer());
    }

    public With<V> build(Consumer<V> finisher) {
      return new Impl<>(this.variableName(), this.internalVariableName(), this.valueSource(), this.action(), finisher);
    }

    private static class Impl<V> extends Base<V> implements With<V> {
      public Impl(String variableName, String internalVariableName, Function<Context, V> valueSource, Action action, Consumer<V> finisher) {
        super(variableName, internalVariableName, valueSource, action, finisher);
      }
    }
  }
}
