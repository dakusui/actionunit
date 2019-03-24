package com.github.dakusui.actionunit.core.context.multiparams;

import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.context.ContextConsumer;

import java.util.function.BiFunction;
import java.util.function.Consumer;

import static com.github.dakusui.actionunit.core.context.ContextFunctions.DEFAULT_PLACE_HOLDER_FORMATTER;
import static com.github.dakusui.actionunit.core.context.ContextFunctions.describeFunctionalObject;
import static java.util.Objects.requireNonNull;

public class MultiParamsContextConsumerBuilder {
  private final String[]                               variableNames;
  private final BiFunction<Consumer, String[], String> descriptionFormatter;

  public MultiParamsContextConsumerBuilder(String... variableNames) {
    this(
        (f, v) -> describeFunctionalObject(f, DEFAULT_PLACE_HOLDER_FORMATTER, v),
        variableNames
    );
  }

  private MultiParamsContextConsumerBuilder(
      BiFunction<Consumer, String[], String> descriptionFormatter,
      String... variableNames) {
    this.variableNames = requireNonNull(variableNames);
    this.descriptionFormatter = requireNonNull(descriptionFormatter);
  }

  public ContextConsumer toContextConsumer(Consumer<Params> consumer) {
    requireNonNull(consumer);
    return new ContextConsumer.Impl(
        () -> descriptionFormatter.apply(consumer, variableNames),
        (Context c) -> consumer.accept(Params.create(c, variableNames))
    );
  }
}
