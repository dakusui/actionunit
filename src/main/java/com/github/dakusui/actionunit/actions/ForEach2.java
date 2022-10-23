package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;

import java.util.Formatter;
import java.util.function.Function;
import java.util.stream.Stream;

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

  class Builder<V> extends Contextful.Builder<Builder<V>, ForEach2<V>, V, Stream<V>> {
    private boolean parallelism;

    public Builder(String variableName, Function<Context, Stream<V>> function) {
      super(variableName, function);
      this.sequentially();
    }

    public Builder<V> parallely() {
      return this.parallelism(true);
    }

    public Builder<V> sequentially() {
      return this.parallelism(false);
    }

    public Builder<V> parallelism(boolean parallelism) {
      this.parallelism = parallelism;
      return this;
    }

    public ForEach2<V> build() {
      return new Impl<>(this.variableName(), this.internalVariableName(), this.valueSource(), this.parallelism, this.action());
    }

    private static class Impl<V> extends Contextful.Base<V, Stream<V>> implements ForEach2<V> {
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