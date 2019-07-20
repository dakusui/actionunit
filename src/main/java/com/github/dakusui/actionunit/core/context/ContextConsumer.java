package com.github.dakusui.actionunit.core.context;

import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.printables.PrintableConsumer;

import java.io.Serializable;
import java.util.Formattable;
import java.util.Formatter;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.github.dakusui.actionunit.utils.InternalUtils.objectToStringIfOverridden;
import static java.util.Objects.requireNonNull;

@FunctionalInterface
public interface ContextConsumer extends Consumer<Context>, Formattable, Serializable {
  @Override
  default void formatTo(Formatter formatter, int flags, int width, int precision) {
    formatter.format(objectToStringIfOverridden(this, () -> "(noname)"));
  }

  @Override
  default ContextConsumer andThen(Consumer<? super Context> after) {
    Objects.requireNonNull(after);
    return (t) -> {
      accept(t);
      after.accept(t);
    };
  }

  static ContextConsumer of(Supplier<String> formatter, Consumer<Context> consumer) {
    return new Impl(formatter, consumer);
  }

  class Impl extends PrintableConsumer<Context> implements ContextConsumer {

    public Impl(Supplier<String> formatter, Consumer<Context> consumer) {
      super(formatter, consumer);
    }

    @Override
    public ContextConsumer andThen(Consumer<? super Context> after) {
      requireNonNull(after);
      return (ContextConsumer) super.andThen(after);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ContextConsumer.Impl createConsumer(Supplier<String> formatter, Consumer<? super Context> consumer) {
      return new ContextConsumer.Impl(formatter, (Consumer<Context>) consumer);
    }
  }

}