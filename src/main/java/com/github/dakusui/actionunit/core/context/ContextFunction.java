package com.github.dakusui.actionunit.core.context;

import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.printables.PrintableFunction;
import com.github.dakusui.printables.Printables;

import java.io.Serializable;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

@FunctionalInterface
public interface ContextFunction<R> extends Function<Context, R>, Serializable, Printable {
  default <V> Function<V, R> compose(Function<? super V, ? extends Context> before) {
    return (Serializable & Function<V, R>) v -> ContextFunction.this.apply(before.apply(v));
  }

  default <V> ContextFunction<V> andThen(Function<? super R, ? extends V> after) {
    requireNonNull(after);
    return (Serializable & ContextFunction<V>) (Context r) -> after.apply(apply(r));
  }

  static <R> ContextFunction<R> of(Supplier<String> formatter, Function<Context, R> function) {
    return new ContextFunction.Impl<>(formatter, function);
  }

  class Impl<R> extends PrintableFunction<Context, R> implements ContextFunction<R> {
    public Impl(Supplier<String> formatter, Function<Context, R> function) {
      super(formatter, function);
    }

    @Override
    public <V> Function<V, R> compose(Function<? super V, ? extends Context> before) {
      return Printables.<V, R>printableFunction(ContextFunction.super.compose(before))
          .describe((Serializable & Supplier<String>) () -> String.format("%s(%s)", getFormatter().get(), before));
    }

    @Override
    public <V> ContextFunction<V> andThen(Function<? super R, ? extends V> after) {
      return new ContextFunction.Impl<>(
          (Serializable & Supplier<String>)() -> String.format("%s(%s)", after, getFormatter().get()),
          toContextFunction(getFunction().andThen(after))
      );
    }

    @SuppressWarnings("unchecked")
    private static <R> ContextFunction<R> toContextFunction(Function<? super Context, ? extends R> function) {
      if (function instanceof ContextFunction)
        return (ContextFunction<R>) function;
      return new ContextFunction.Impl<>(function::toString, (Function<Context, R>) function);
    }
  }
}
