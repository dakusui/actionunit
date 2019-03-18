package com.github.dakusui.actionunit.core.context;

import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.printables.PrintableFunction;

import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

@FunctionalInterface
public interface ContextFunction<R> extends Function<Context, R>, Printable {
  default <V> Function<V, R> compose(Function<? super V, ? extends Context> before) {
    throw new UnsupportedOperationException();
  }

  default <V> ContextFunction<V> andThen(Function<? super R, ? extends V> after) {
    requireNonNull(after);
    return (Context r) -> after.apply(apply(r));
  }


  static <R> ContextFunction<R> of(Supplier<String> formatter, Function<Context, R> function) {
    return new ContextFunction.Impl<R>(formatter, function);
  }

  class Impl<R> extends PrintableFunction<Context, R> implements ContextFunction<R> {
    public Impl(Supplier<String> formatter, Function<Context, R> function) {
      super(formatter, function);
    }

    @Override
    public <V> Function<V, R> compose(Function<? super V, ? extends Context> before) {
      return ContextFunction.super.compose(before);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> ContextFunction<V> andThen(Function<? super R, ? extends V> after) {
      return new ContextFunction.Impl<>(
          () -> String.format("%s(%s)", after, getFormatter().get()),
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
