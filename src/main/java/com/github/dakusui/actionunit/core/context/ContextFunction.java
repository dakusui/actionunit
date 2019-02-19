package com.github.dakusui.actionunit.core.context;

import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.printables.PrintableFunction;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import static com.github.dakusui.actionunit.core.context.ContextFunctions.PLACE_HOLDER_FORMATTER;
import static com.github.dakusui.actionunit.core.context.ContextFunctions.describeFunctionalObject;
import static com.github.dakusui.printables.Printables.function;
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


  static <T, R> ContextFunction<R> of(String variableName, Function<T, R> function) {
    return ContextFunctions.<R>contextFunctionFor(variableName)
        .with(function(
            (Params params) -> function.apply(params.valueOf(variableName))
        ).describe(
            function.toString()
        ));
  }

  class Impl<R> extends PrintableFunction<Context, R> implements ContextFunction<R> {
    Impl(Supplier<String> formatter, Function<Context, R> function) {
      super(formatter, function);
    }

    @Override
    public <V> Function<V, R> compose(Function<? super V, ? extends Context> before) {
      return ContextFunction.super.compose(before);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> ContextFunction.Impl<V> andThen(Function<? super R, ? extends V> after) {
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

  class Builder<R> {
    private final String[]                               variableNames;
    private final BiFunction<Function, String[], String> descriptionFormatter;

    public Builder(String... variableNames) {
      this(
          (f, v) -> describeFunctionalObject(f, PLACE_HOLDER_FORMATTER, v),
          variableNames
      );
    }

    public Builder(
        BiFunction<Function, String[], String> descriptionFormatter,
        String... variableNames) {
      this.descriptionFormatter = requireNonNull(descriptionFormatter);
      this.variableNames = requireNonNull(variableNames);
    }

    public Builder(
        IntFunction<String> placeHolderFormatter,
        String... variableNames) {
      this((f, v) -> describeFunctionalObject(f, placeHolderFormatter, v), variableNames);
    }

    public ContextFunction<R> with(Function<Params, R> function) {
      requireNonNull(function);
      return new Impl<>(
          () -> descriptionFormatter.apply(function, variableNames),
          (Context c) -> function.apply(Params.create(c, variableNames)));
    }
  }
}
