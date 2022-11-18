package com.github.dakusui.actionunit.core.context.multiparams;

import com.github.dakusui.actionunit.actions.ContextVariable;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.context.ContextConsumer;

import java.util.function.BiFunction;
import java.util.function.Consumer;

import static com.github.dakusui.actionunit.actions.cmd.PlaceHolderFormatter.DEFAULT_PLACE_HOLDER_FORMATTER;
import static com.github.dakusui.actionunit.core.context.ContextFunctions.describeFunctionalObject;
import static java.util.Objects.requireNonNull;

public class MultiParamsContextConsumerBuilder {
  private final ContextVariable[]                                  contextVariables;
  private final BiFunction<Consumer<?>, ContextVariable[], String> descriptionFormatter;

  public MultiParamsContextConsumerBuilder(ContextVariable... contextVariables) {
    this(
        (Consumer<?> f, ContextVariable[] v) -> describeFunctionalObject(f, DEFAULT_PLACE_HOLDER_FORMATTER.apply(v), v),
        contextVariables
    );
  }

  private MultiParamsContextConsumerBuilder(
      BiFunction<Consumer<?>, ContextVariable[], String> descriptionFormatter,
      ContextVariable... contextVariables) {
    this.contextVariables = requireNonNull(contextVariables);
    this.descriptionFormatter = requireNonNull(descriptionFormatter);
  }

  public Consumer<Context> toContextConsumer(Consumer<Params> consumer) {
    requireNonNull(consumer);
    return new ContextConsumer.Impl(
        () -> descriptionFormatter.apply(consumer, contextVariables),
        (Context c) -> consumer.accept(Params.create(c, contextVariables))
    );
  }
}
