package com.github.dakusui.actionunit.core.context.multiparams;

import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.context.ContextFunctions;
import com.github.dakusui.actionunit.core.context.ContextPredicate;

import java.util.function.BiFunction;
import java.util.function.Predicate;

import static com.github.dakusui.actionunit.core.context.ContextFunctions.PLACE_HOLDER_FORMATTER;
import static com.github.dakusui.actionunit.core.context.ContextFunctions.describeFunctionalObject;
import static com.github.dakusui.printables.Printables.predicate;
import static java.util.Objects.requireNonNull;

public class MultiParamsPredicateBuilder {
  private final String[]                                variableNames;
  private final BiFunction<Predicate, String[], String> descriptionFormatter;

  public MultiParamsPredicateBuilder(String... variableNames) {
    this(
        (Predicate f, String[] v) -> describeFunctionalObject(f, PLACE_HOLDER_FORMATTER, v),
        variableNames
    );
  }

  private MultiParamsPredicateBuilder(
      BiFunction<Predicate, String[], String> descriptionFormatter,
      String... variableNames) {
    this.variableNames = requireNonNull(variableNames);
    this.descriptionFormatter = requireNonNull(descriptionFormatter);
  }

  public static <T> ContextPredicate of(String variableName, Predicate<T> predicate) {
    return ContextFunctions.contextPredicateFor(variableName)
        .with(
            predicate((Params params) -> predicate.test(params.valueOf(variableName)))
                .describe(predicate.toString())
        );
  }

  public ContextPredicate with(Predicate<Params> predicate) {
    requireNonNull(predicate);
    return new ContextPredicate.Impl(
        () -> descriptionFormatter.apply(predicate, variableNames),
        (Context c) -> predicate.test(Params.create(c, variableNames)));
  }
}
