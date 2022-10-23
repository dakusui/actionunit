package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;

import java.util.Formatter;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.dakusui.actionunit.core.ActionSupport.simple;
import static com.github.dakusui.actionunit.utils.InternalUtils.toStringIfOverriddenOrNoname;

public interface ForEach2<V> extends Contextful<Stream<V>> {
  @Override
  default void accept(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  default void formatTo(Formatter formatter, int flags, int width, int precision) {
    formatter.format("forEach:" + variableName() + ":" + toStringIfOverriddenOrNoname(valueSource()));
  }

  boolean isParallel();

  class Builder<V> extends Contextful.Builder<Builder<V>, ForEach2<V>, Stream<V>> {
    private boolean parallelism;

    public Builder(String variableName, Function<Context, Stream<V>> function) {
      super(variableName, function);
      sequentially();
    }

    public Builder<V> parallely() {
      return this.parallelism(false);
    }

    public Builder<V> sequentially() {
      return this.parallelism(true);
    }

    public Builder<V> parallelism(boolean parallelism) {
      this.parallelism = parallelism;
      return this;
    }


    public Action toAction(Consumer<V> consumer) {
      return simple(toStringIfOverriddenOrNoname(consumer) + ":" + variableName(),
          (Context c) -> variableReferenceConsumer(consumer).accept(c));
    }

    public V contextVariable(Context context) {
      return this.contextVariableValue(context);
    }

    private Consumer<Context> variableReferenceConsumer(Consumer<V> consumer) {
      return (Context context) -> consumer.accept(context.valueOf(internalVariableName()));
    }

    public ForEach2<V> build() {
      return new Impl<>(this.variableName(), this.internalVariableName(), this.valueSource(), this.parallelism, this.action());
    }

    private static class Impl<V> extends Contextful.Base<Stream<V>> implements ForEach2<V> {
      private final boolean parallelism;

      public Impl(String variableName, String internalVariableName, Function<Context, Stream<V>> valueSource, boolean parallelism, Action action) {
        super(variableName, internalVariableName, valueSource, action);
        this.parallelism = parallelism;
      }

      @Override
      public boolean isParallel() {
        return this.parallelism;
      }
    }
  }
}
