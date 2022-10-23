package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;

import java.util.Formatter;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.github.dakusui.actionunit.core.ActionSupport.simple;
import static com.github.dakusui.actionunit.core.context.FormattableConsumer.nopConsumer;
import static com.github.dakusui.actionunit.utils.InternalUtils.toStringIfOverriddenOrNoname;
import static com.github.dakusui.printables.PrintableFunctionals.*;
import static java.util.Objects.requireNonNull;

/**
 * An interface to access a context variable safely.
 * An instance of this interface corresponds to single context variable.
 *
 * @param <V> A type of variable through which this action interacts with another.
 * @see Context
 */
public interface With<V> extends Contextful<V> {
  /**
   * Returns a "close" action which takes care of "clean up"
   *
   * @return A "close" action.
   */
  Optional<Action> close();

  @Override
  default void accept(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  default void formatTo(Formatter formatter, int flags, int width, int precision) {
    formatter.format("with:" + variableName() + ":" + toStringIfOverriddenOrNoname(valueSource()));
  }

  class Builder<V> extends Contextful.Builder<Builder<V>, With<V>, V, V> {

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
    public Action updateContextVariableWith(Function<V, V> function) {
      return simple(
          "update:" + variableName() + "*",
          printableConsumer((Context c) -> variableUpdateFunction(function).apply(c)).describe(toStringIfOverriddenOrNoname(function)));
    }


    public With<V> build() {
      return build(nopConsumer());
    }

    public With<V> build(Consumer<V> finisher) {
      return new Impl<>(this.variableName(), this.internalVariableName(), this.valueSource(), this.action(), finisher);
    }

    public <W> Builder<W> nest(Function<V, W> function) {
      return new Builder<>(nextVariableName(variableName()), function(function));
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
    private Function<Context, V> variableUpdateFunction(Function<V, V> function) {
      return printableFunction(
          (Context context) -> {
            V ret = function.apply(contextVariable(context));
            context.assignTo(internalVariableName(), ret);
            return ret;
          })
          .describe(toStringIfOverriddenOrNoname(function));
    }

    private static class Impl<V> extends Base<V, V> implements With<V> {
      private final Action end;

      public Impl(String variableName, String internalVariableName, Function<Context, V> valueSource, Action action, Consumer<V> finisher) {
        super(variableName, internalVariableName, valueSource, action);
        this.end = simple(String.format("done:%s", finisher),
            printableConsumer((Context context) -> {
              V variable = context.valueOf(internalVariableName);
              context.unassign(internalVariableName); // Un-assign first. Otherwise, finisher may fail.
              if (finisher != null)
                finisher.accept(variable);
            }).describe(String.format("cleanUp:%s", variableName)));
      }

      @Override
      public Optional<Action> close() {
        return Optional.ofNullable(end);
      }

    }
  }
}
