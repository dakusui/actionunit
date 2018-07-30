package com.github.dakusui.actionunit.core;

import java.util.Formattable;
import java.util.Formatter;
import java.util.function.Consumer;

@FunctionalInterface
public interface ContextConsumer extends Consumer<Context>, Formattable {
  @Override
  default void formatTo(Formatter formatter, int flags, int width, int precision) {
    formatter.format("(noname)");
  }
}
