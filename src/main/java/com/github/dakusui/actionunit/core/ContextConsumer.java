package com.github.dakusui.actionunit.core;

import com.github.dakusui.actionunit.core.ContextFunctions.Params;
import com.github.dakusui.printables.PrintableConsumer;

import java.text.MessageFormat;
import java.util.Formattable;
import java.util.Formatter;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.github.dakusui.actionunit.utils.InternalUtils.suppressObjectToString;
import static com.github.dakusui.printables.Printables.consumer;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

@FunctionalInterface
public interface ContextConsumer extends Consumer<Context>, Formattable {
  @Override
  default void formatTo(Formatter formatter, int flags, int width, int precision) {
    formatter.format(suppressObjectToString(this.toString()));
  }

  @Override
  default ContextConsumer andThen(Consumer<? super Context> after) {
    Objects.requireNonNull(after);
    return (t) -> {
      accept(t);
      after.accept(t);
    };
  }

  static <T> ContextConsumer of(String variableName, Consumer<T> consumer) {
    return ContextFunctions.contextConsumerFor(variableName)
        .with(
            consumer((Params params) -> consumer.accept(params.valueOf(variableName)))
                .describe(consumer.toString()));
  }

  class Impl extends PrintableConsumer<Context> implements ContextConsumer {

    Impl(Supplier<String> formatter, Consumer<Context> consumer) {
      super(formatter, consumer);
    }

    @Override
    public ContextConsumer andThen(Consumer<? super Context> after) {
      requireNonNull(after);
      return (ContextConsumer) super.andThen(after);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ContextConsumer.Impl createConsumer(Supplier<String> formatter, Consumer<? super Context> predicate) {
      return new ContextConsumer.Impl(formatter, (Consumer<Context>) predicate);
    }
  }

  class Builder {
    private final String[]                               variableNames;
    private final BiFunction<Consumer, String[], String> descriptionFormatter;

    public Builder(String... variableNames) {
      this(
          (Consumer c, String[] v) -> format(
              "(%s)->%s",
              String.join(",", variableNames),
              MessageFormat.format(suppressObjectToString(c.toString()), (Object[]) v)),
          variableNames
      );
    }

    Builder(BiFunction<Consumer, String[], String> descriptionFormatter, String... variableNames) {
      this.variableNames = requireNonNull(variableNames);
      this.descriptionFormatter = requireNonNull(descriptionFormatter);
    }

    public ContextConsumer with(Consumer<Params> consumer) {
      requireNonNull(consumer);
      return new Impl(
          () -> descriptionFormatter.apply(consumer, variableNames),
          (Context c) -> consumer.accept(Params.create(c, variableNames))
      );
    }
  }
}