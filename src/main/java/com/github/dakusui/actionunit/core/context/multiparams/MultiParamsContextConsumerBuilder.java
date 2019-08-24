package com.github.dakusui.actionunit.core.context.multiparams;

import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.context.ContextConsumer;

import java.io.Serializable;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.github.dakusui.actionunit.core.context.ContextFunctions.DEFAULT_PLACE_HOLDER_FORMATTER;
import static com.github.dakusui.actionunit.core.context.ContextFunctions.describeFunctionalObject;
import static java.util.Objects.requireNonNull;

public class MultiParamsContextConsumerBuilder implements Serializable {
  private final String[]                               variableNames;
  private final BiFunction<Consumer, String[], String> descriptionFormatter;

  public MultiParamsContextConsumerBuilder(String... variableNames) {
    this(
        (Serializable & BiFunction<Consumer, String[], String>) (f, v) -> describeFunctionalObject(f, DEFAULT_PLACE_HOLDER_FORMATTER.apply(v), v),
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
        (Serializable & Supplier<String>) () -> descriptionFormatter.apply(consumer, variableNames),
        (Serializable & Consumer<Context>) (Context c) -> consumer.accept(Params.create(c, variableNames))
    );
  }
}
