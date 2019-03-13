package com.github.dakusui.actionunit.core.context.multiparams;

import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.context.ContextFunction;

import java.util.function.BiFunction;
import java.util.function.Function;

import static com.github.dakusui.actionunit.core.context.ContextFunctions.PLACE_HOLDER_FORMATTER;
import static com.github.dakusui.actionunit.core.context.ContextFunctions.describeFunctionalObject;
import static java.util.Objects.requireNonNull;

public class MultiParamsFunctionBuilder<R> {
  private final String[]                               variableNames;
  private final BiFunction<Function, String[], String> descriptionFormatter;

  public MultiParamsFunctionBuilder(String... variableNames) {
    this(
        (f, v) -> describeFunctionalObject(f, PLACE_HOLDER_FORMATTER, v),
        variableNames
    );
  }

  private MultiParamsFunctionBuilder(
      BiFunction<Function, String[], String> descriptionFormatter,
      String... variableNames) {
    this.descriptionFormatter = requireNonNull(descriptionFormatter);
    this.variableNames = requireNonNull(variableNames);
  }

  public ContextFunction<R> with(Function<Params, R> function) {
    requireNonNull(function);
    return new ContextFunction.Impl<>(
        () -> descriptionFormatter.apply(function, variableNames),
        (Context c) -> function.apply(Params.create(c, variableNames)));
  }
}