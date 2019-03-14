package com.github.dakusui.actionunit.core.context.multiparams;

import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.context.ContextPredicate;

import java.util.function.BiFunction;
import java.util.function.Predicate;

import static com.github.dakusui.actionunit.core.context.ContextFunctions.PLACE_HOLDER_FORMATTER;
import static com.github.dakusui.actionunit.core.context.ContextFunctions.describeFunctionalObject;
import static java.util.Objects.requireNonNull;

public class MultiParamsContextPredicateBuilder {
  private final String[]                                variableNames;
  private final BiFunction<Predicate, String[], String> descriptionFormatter;

  public MultiParamsContextPredicateBuilder(String... variableNames) {
    this(
        (Predicate f, String[] v) -> describeFunctionalObject(f, PLACE_HOLDER_FORMATTER, v),
        variableNames
    );
  }

  private MultiParamsContextPredicateBuilder(
      BiFunction<Predicate, String[], String> descriptionFormatter,
      String... variableNames) {
    this.variableNames = requireNonNull(variableNames);
    this.descriptionFormatter = requireNonNull(descriptionFormatter);
  }

  public ContextPredicate toContextPredicate(Predicate<Params> predicate) {
    requireNonNull(predicate);
    return new ContextPredicate.Impl(
        () -> descriptionFormatter.apply(predicate, variableNames),
        (Context c) -> predicate.test(Params.create(c, variableNames)));
  }
}
