package com.github.dakusui.actionunit.core.context.multiparams;

import com.github.dakusui.actionunit.actions.ContextVariable;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.context.ContextFunction;

import java.util.function.BiFunction;
import java.util.function.Function;

import static com.github.dakusui.actionunit.actions.cmd.CommanderConfig.DEFAULT_PLACE_HOLDER_FORMATTER;
import static com.github.dakusui.actionunit.core.context.ContextFunctions.describeFunctionalObject;
import static java.util.Objects.requireNonNull;

public class MultiParamsContextFunctionBuilder<R> {
  private final ContextVariable[]                                     variables;
  private final BiFunction<Function<?, ?>, ContextVariable[], String> descriptionFormatter;

  public MultiParamsContextFunctionBuilder(ContextVariable... variables) {
    this(
        (f, v) -> describeFunctionalObject(f, DEFAULT_PLACE_HOLDER_FORMATTER.apply(v), v),
        variables
    );
  }

  private MultiParamsContextFunctionBuilder(
      BiFunction<Function<?, ?>, ContextVariable[], String> descriptionFormatter, ContextVariable... variables) {
    this.descriptionFormatter = requireNonNull(descriptionFormatter);
    this.variables = requireNonNull(variables);
  }

  public Function<Context, R> toContextFunction(Function<Params, R> function) {
    requireNonNull(function);
    return new ContextFunction.Impl<>(
        () -> descriptionFormatter.apply(function, variables),
        (Context c) -> function.apply(Params.create(c, variables)));
  }
}
