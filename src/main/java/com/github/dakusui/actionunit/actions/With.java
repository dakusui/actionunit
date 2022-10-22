package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.ActionSupport;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.context.FormattableConsumer;
import com.github.dakusui.printables.PrintableFunctionals;

import java.util.Formatter;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.dakusui.actionunit.core.context.FormattableConsumer.nopConsumer;
import static com.github.dakusui.actionunit.utils.InternalUtils.toStringIfOverriddenOrNoname;
import static java.util.Objects.requireNonNull;

/**
 * An interface to access a context variable safely.
 * An instance of this interface corresponds to single context variable.
 *
 * @param <V> A type of variable through which this action interacts with another.
 *
 * @see Context
 */
public interface With<V> extends Action {
  @Override
  default void accept(Visitor visitor) {
    visitor.visit(this);
  }

  /**
   * A function to provide a value referenced from inside an action returned by the
   * {@link this#action()} method.
   *
   * @return A function to provide a value for an action.
   */
  Function<Context, V> valueSource();

  Action action();

  Action close();

  /**
   * A name of the variable.
   * The string returned by this method is used only for printing an action tree.
   * To identify a variable, a string returned by {@link this#internalVariableName()}.
   *
   * @return A human-readable variable name.
   */
  String variableName();

  String internalVariableName();


  @Override
  default void formatTo(Formatter formatter, int flags, int width, int precision) {
    formatter.format("with:" + variableName() + ":" + toStringIfOverriddenOrNoname(valueSource()));
  }

  class Builder<V> extends Action.Builder<With<V>> {

    private final Function<Context, V> valueSource;
    private       Action               action;
    private final String               internalVariableName;
    private final String               variableName;

    public Builder(String variableName, Function<Context, V> function) {
      this.valueSource = requireNonNull(function);
      this.internalVariableName = variableName + ":" + System.identityHashCode(this);
      this.variableName = variableName;
    }

    public Builder<V> action(Action action) {
      this.action = requireNonNull(action);
      return this;
    }

    public Builder<V> action(Function<Builder<V>, Action> action) {
      return this.action(action.apply(this));
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
      return ActionSupport.simple(
          toStringIfOverriddenOrNoname(function) + ":" + variableName + "*",
          (Context c) -> variableUpdateFunction(function).apply(c));
    }

    /**
     * Creates an action that consumes the context variable.
     * @param consumer
     * @return
     */
    public Action referenceVariable(Consumer<V> consumer) {
      return ActionSupport.simple(
          toStringIfOverriddenOrNoname(consumer) + ":" + variableName,
          (Context c) -> variableReferenceConsumer(consumer).accept(c));
    }

    public <W> Builder<W> nest(Function<V, W> function) {
      return new Builder<>(nextVariableName(variableName), function(function));
    }

    private static String nextVariableName(String variableName) {
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
      return PrintableFunctionals.printableFunction(
              (Context context) -> {
                V ret = function.apply(context.valueOf(internalVariableName));
                context.assignTo(internalVariableName, ret);
                return ret;
              })
          .describe("XYZ");
    }

    private Consumer<Context> variableReferenceConsumer(Consumer<V> consumer) {
      return PrintableFunctionals.printableConsumer(
              (Context context) -> consumer.accept(context.valueOf(internalVariableName)))
          .describe("XYZ");
    }


    private <W> Function<Context, W> function(Function<V, W> function) {
      return PrintableFunctionals.printableFunction(
              (Context context) -> function.apply(context.valueOf(internalVariableName)))
          .describe(() -> variableName + ":" + toStringIfOverriddenOrNoname(function));
    }

    public Consumer<Context> consumer(Consumer<V> consumer) {
      return context -> consumer.accept(context.valueOf(internalVariableName));
    }

    public Predicate<Context> predicate(Predicate<V> predicate) {
      return PrintableFunctionals.printablePredicate(
              (Context context) -> predicate.test(context.valueOf(internalVariableName)))
          .describe(() -> variableName + ":" + toStringIfOverriddenOrNoname(predicate));
    }

    public With<V> build() {
      return build(nopConsumer());
    }

    public With<V> build(Consumer<V> finisher) {
      return new With<V>() {
        final String variableName = Builder.this.variableName;

        final String internalVariableName = Builder.this.internalVariableName;

        final Function<Context, V> valueSource = Builder.this.valueSource;

        final Action action = Builder.this.action;
        private final Action end = ActionSupport.simple(String.format("done:%s", finisher),
            PrintableFunctionals.printableConsumer(new FormattableConsumer<Context>() {
              @Override
              public void accept(Context context) {
                V variable = context.valueOf(Builder.this.internalVariableName);
                context.unassign(Builder.this.internalVariableName); // Unassign first. Otherwise, finisher may fail.
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
        public Action close() {
          return end;
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
