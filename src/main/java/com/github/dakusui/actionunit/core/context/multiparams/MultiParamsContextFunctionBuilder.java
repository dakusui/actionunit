package com.github.dakusui.actionunit.core.context.multiparams;

import com.github.dakusui.actionunit.actions.ContextVariable;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.context.ContextFunction;

import java.util.function.BiFunction;
import java.util.function.Function;

import static com.github.dakusui.actionunit.core.context.ContextFunctions.DEFAULT_PLACE_HOLDER_FORMATTER;
import static com.github.dakusui.actionunit.core.context.ContextFunctions.describeFunctionalObject;
import static java.util.Objects.requireNonNull;

public class MultiParamsContextFunctionBuilder<R> {
  private final ContextVariable[]                                     variableNames;
  private final BiFunction<Function<?, ?>, ContextVariable[], String> descriptionFormatter;

  public MultiParamsContextFunctionBuilder(ContextVariable... variableNames) {
    this(
        (f, v) -> describeFunctionalObject(f, DEFAULT_PLACE_HOLDER_FORMATTER.apply(v), v),
        variableNames
    );
  }

  private MultiParamsContextFunctionBuilder(
      BiFunction<Function<?, ?>, ContextVariable[], String> descriptionFormatter,
      ContextVariable... variableNames) {
    this.descriptionFormatter = requireNonNull(descriptionFormatter);
    this.variableNames = requireNonNull(variableNames);
  }

  public ContextFunction<R> toContextFunction(Function<Params, R> function) {
    requireNonNull(function);
    return new ContextFunction.Impl<>(
        () -> descriptionFormatter.apply(function, variableNames),
        (Context c) -> function.apply(Params.create(c, variableNames)));
  }
}
