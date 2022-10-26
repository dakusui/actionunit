package com.github.dakusui.actionunit.core.context.multiparams;

import com.github.dakusui.actionunit.actions.ContextVariable;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.context.ContextPredicate;

import java.util.function.BiFunction;
import java.util.function.Predicate;

import static com.github.dakusui.actionunit.actions.cmd.CommanderConfig.DEFAULT_PLACE_HOLDER_FORMATTER;
import static com.github.dakusui.actionunit.core.context.ContextFunctions.describeFunctionalObject;
import static java.util.Objects.requireNonNull;

public class MultiParamsContextPredicateBuilder {
  private final ContextVariable[]                                   contextVariables;
  private final BiFunction<Predicate<?>, ContextVariable[], String> descriptionFormatter;

  public MultiParamsContextPredicateBuilder(ContextVariable... contextVariables) {
    this(
        (Predicate<?> f, ContextVariable[] v) -> describeFunctionalObject(f, DEFAULT_PLACE_HOLDER_FORMATTER.apply(v), v),
        contextVariables
    );
  }

  private MultiParamsContextPredicateBuilder(
      BiFunction<Predicate<?>, ContextVariable[], String> descriptionFormatter,
      ContextVariable... contextVariables) {
    this.contextVariables = requireNonNull(contextVariables);
    this.descriptionFormatter = requireNonNull(descriptionFormatter);
  }

  public Predicate<Context> toContextPredicate(Predicate<Params> predicate) {
    requireNonNull(predicate);
    return new ContextPredicate.Impl(
        () -> descriptionFormatter.apply(predicate, contextVariables),
        (Context c) -> predicate.test(Params.create(c, contextVariables)));
  }
}
