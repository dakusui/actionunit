package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.dakusui.actionunit.core.ActionSupport.simple;
import static com.github.dakusui.actionunit.utils.InternalUtils.toStringIfOverriddenOrNoname;
import static com.github.dakusui.printables.PrintableFunctionals.*;
import static java.util.Objects.requireNonNull;

public interface Contextful<S> extends Action, ContextVariable {
  /**
   * A function to provide a value referenced from inside an action returned by the
   * {@link Contextful#action()} method.
   *
   * @return A function to provide a value for an action.
   */
  Function<Context, S> valueSource();

  /**
   * Returns a main action.
   *
   * @return A main action.
   */
  Action action();

  @Override
  String variableName();

  @Override
  String internalVariableName();

  abstract class Base<V, S> implements Contextful<S> {
    private final String variableName;

    private final String internalVariableName;

    private final Function<Context, S> valueSource;

    private final Action action;


    public Base(String variableName, final String internalVariableName, Function<Context, S> valueSource, Action action) {
      this.variableName = variableName;
      this.internalVariableName = internalVariableName;
      this.valueSource = valueSource;
      this.action = action;
    }

    @Override
    public Function<Context, S> valueSource() {
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

  abstract class Builder<
      B extends Builder<B, A, V, S>,
      A extends Contextful<S>,
      V,
      S>
      extends Action.Builder<A>
      implements ContextVariable {
    private final Function<Context, S> valueSource;
    private final String               internalVariableName;
    private final String               variableName;

    private Action action;

    protected Builder(String variableName, Function<Context, S> function) {
      this.variableName = requireNonNull(variableName);
      this.internalVariableName = composeInternalVariableName(variableName);
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

    public A perform(Action action) {
      return action(action).build();
    }

    public A perform(Function<B, Action> action) {
      return this.action(action).build();
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

    public Function<Context, S> valueSource() {
      return this.valueSource;
    }

    /**
     * Creates an action that consumes the context variable.
     *
     * @param consumer A consumer that processes the context variable.
     * @return A created action.
     */
    public Action toAction(Consumer<V> consumer) {
      return simple("action:" + variableName(),
          printableConsumer((Context c) -> variableReferenceConsumer(consumer).accept(c)).describe(toStringIfOverriddenOrNoname(consumer)));
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

    public V contextVariable(Context context) {
      return contextVariableValue(context);
    }

    public String toString() {
      return variableName();
    }

    protected <VV> VV contextVariableValue(Context context) {
      return context.valueOf(internalVariableName());
    }

    protected String composeInternalVariableName(String variableName) {
      return this.getClass().getEnclosingClass().getCanonicalName() + ":" + variableName + ":" + System.identityHashCode(this);
    }
    private Consumer<Context> variableReferenceConsumer(Consumer<V> consumer) {
      return (Context context) -> consumer.accept(context.valueOf(internalVariableName()));
    }

    private static <V, W> Function<Context, W> toContextFunction(Builder<?, ?, V, ?> builder, Function<V, W> function) {
      return printableFunction((Context context) -> function.apply(context.valueOf(builder.internalVariableName())))
          .describe(toStringIfOverriddenOrNoname(function));
    }

    private static <V> Consumer<Context> toContextConsumer(Builder<?, ?, V, ?> builder, Consumer<V> consumer) {
      return printableConsumer((Context context) -> consumer.accept(context.valueOf(builder.internalVariableName())))
          .describe(toStringIfOverriddenOrNoname(consumer));
    }

    private static <V> Predicate<Context> toContextPredicate(Builder<?, ?, V, ?> builder, Predicate<V> predicate) {
      return printablePredicate(
          (Context context) -> predicate.test(context.valueOf(builder.internalVariableName())))
          .describe(() -> builder.variableName() + ":" + toStringIfOverriddenOrNoname(predicate));
    }

  }
}
