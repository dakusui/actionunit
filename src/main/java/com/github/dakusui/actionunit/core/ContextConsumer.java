package com.github.dakusui.actionunit.core;

import com.github.dakusui.printables.PrintableConsumer;

import java.text.MessageFormat;
import java.util.Formattable;
import java.util.Formatter;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

@FunctionalInterface
public interface ContextConsumer extends Consumer<Context>, Formattable {
  @Override
  default void formatTo(Formatter formatter, int flags, int width, int precision) {
    formatter.format(this.toString());
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
    return ContextFunctions.contextConsumerFor(variableName).with(consumer);
  }

  class Impl extends PrintableConsumer<Context> implements ContextConsumer {

    Impl(Supplier<String> s, Consumer<Context> consumer) {
      super(s, consumer);
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
    private final String                               variableName;
    private final BiFunction<Consumer, String, String> descriptionFormatter;

    Builder(String variableName) {
      this(variableName,
          (Consumer c, String v) -> format(
              "%s:[%s]",
              variableName,
              MessageFormat.format(c.toString(), v)));
    }

    Builder(String variableName, BiFunction<Consumer, String, String> descriptionFormatter) {
      this.variableName = requireNonNull(variableName);
      this.descriptionFormatter = requireNonNull(descriptionFormatter);
    }

    public <T> ContextConsumer with(Consumer<T> consumer) {
      requireNonNull(consumer);
      return new Impl(
          () -> descriptionFormatter.apply(consumer, variableName),
          c -> consumer.accept(c.valueOf(variableName))
      );
    }
  }
}