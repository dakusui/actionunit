package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;

import java.util.Formatter;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.dakusui.actionunit.core.ActionSupport.simple;
import static com.github.dakusui.actionunit.core.context.FormattableConsumer.nopConsumer;
import static com.github.dakusui.actionunit.utils.InternalUtils.toStringIfOverriddenOrNoname;
import static com.github.dakusui.printables.PrintableFunctionals.*;

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

    public <W> Function<Context, W> function(Function<V, W> function) {
      return toContextFunction(this, function);

    }

    public Consumer<Context> consumer(Consumer<V> consumer) {
      return toContextConsumer(this, consumer);
    }

    public Predicate<Context> predicate(Predicate<V> predicate) {
      return toContextPredicate(this, predicate);
    }

    public With<V> build() {
      return build(nopConsumer());
    }

    public With<V> build(Consumer<V> finisher) {
      return new Impl<>(this.variableName(), this.internalVariableName(), this.valueSource(), this.action(), finisher);
    }

    private Function<Context, V> variableUpdateFunction(Function<V, V> function) {
      return printableFunction(
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

    private static <V, W> Function<Context, W> toContextFunction(Builder<V> builder, Function<V, W> function) {
      return printableFunction((Context context) -> function.apply(context.valueOf(builder.internalVariableName())))
          .describe(toStringIfOverriddenOrNoname(function));
    }

    private static <V> Consumer<Context> toContextConsumer(Builder<V> builder, Consumer<V> consumer) {
      return printableConsumer((Context context) -> consumer.accept(context.valueOf(builder.internalVariableName())))
          .describe(toStringIfOverriddenOrNoname(consumer));
    }

    private static <V> Predicate<Context> toContextPredicate(Builder<V> builder, Predicate<V> predicate) {
      return printablePredicate(
          (Context context) -> predicate.test(context.valueOf(builder.internalVariableName())))
          .describe(() -> builder.variableName() + ":" + toStringIfOverriddenOrNoname(predicate));
    }

    private static class Impl<V> extends Base<V> implements With<V> {
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
