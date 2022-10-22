package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;

import java.util.Formatter;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.dakusui.actionunit.core.context.FormattableConsumer.nopConsumer;
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

  class Builder<V> extends Contextful.Builder<Builder<V>, ForEach2<V>, Stream<V>> {
    public Builder(String variableName, Function<Context, Stream<V>> function) {
      super(variableName, function);
    }

    public ForEach2<V> build() {
      return new Impl<>(this.variableName(), this.internalVariableName(), this.valueSource(), this.action());
    }

    private static class Impl<V> extends Contextful.Base<Stream<V>> implements ForEach2<V> {
      public Impl(String variableName, String internalVariableName, Function<Context, Stream<V>> valueSource, Action action) {
        super(variableName, internalVariableName, valueSource, action);
      }
    }
  }
}
