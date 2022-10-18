package com.github.dakusui.actionunit.core.context;

import com.github.dakusui.actionunit.utils.InternalUtils;

import java.util.Formattable;
import java.util.Formatter;
import java.util.function.Consumer;

@FunctionalInterface
public interface FormattableConsumer<T> extends Consumer<T>, Formattable {
  FormattableConsumer<?> NOP = new FormattableConsumer<Object>() {
    @Override
    public void accept(Object t) {
    }

    public void formatTo(Formatter formatter, int i, int i1, int i2) {
      formatter.format("%s", "(nop)");
    }
  };

  @Override
  default void formatTo(Formatter formatter, int i, int i1, int i2) {
    formatter.format("%s", InternalUtils.toStringIfOverriddenOrNoname(this));
  }

  @SuppressWarnings("unchecked")
  static <T> FormattableConsumer<T> nopConsumer() {
    return (FormattableConsumer<T>) NOP;
  }
}
