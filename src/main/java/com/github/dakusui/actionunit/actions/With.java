package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.context.FormattableConsumer;
import com.github.dakusui.printables.PrintableFunctionals;

import java.util.Formatter;
import java.util.Optional;
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
  /**
   * A function to provide a value referenced from inside an action returned by the
   * {@link Contextful#action()} method.
   *
   * @return A function to provide a value for an action.
   */
  Function<Context, V> valueSource();

  @Override
  default void accept(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  default void formatTo(Formatter formatter, int flags, int width, int precision) {
    formatter.format("with:" + variableName() + ":" + toStringIfOverriddenOrNoname(valueSource()));
  }

  class Builder<V> extends Contextful.Builder<Builder<V>, With<V>, V> {

    private final Function<Context, V> valueSource;

    public Builder(String variableName, Function<Context, V> function) {
      super(variableName);
      this.valueSource = requireNonNull(function);
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
      return new With<V>() {
        private final String variableName = Builder.this.variableName();

        private final String internalVariableName = Builder.this.internalVariableName();

        private final Function<Context, V> valueSource = Builder.this.valueSource;

        private final Action action = Builder.this.action();

        private final Action end = finisher == null ? null : simple(String.format("done:%s", finisher),
            PrintableFunctionals.printableConsumer(new FormattableConsumer<Context>() {
              @Override
              public void accept(Context context) {
                V variable = context.valueOf(Builder.this.internalVariableName());
                context.unassign(Builder.this.internalVariableName()); // Unassign first. Otherwise, finisher may fail.
                finisher.accept(variable);
              }

              @Override
              public void formatTo(Formatter formatter, int i, int i1, int i2) {
                formatter.format("%s", toStringIfOverriddenOrNoname(finisher));
              }
            }).describe(String.format("cleanUp:%s", variableName)));

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
      };
    }
  }
}
