package com.github.dakusui.actionunit.core;

import java.util.Formattable;
import java.util.Formatter;
import java.util.function.Function;
import java.util.stream.Stream;

public interface StreamGenerator<T> extends Function<Context, Stream<T>>, Formattable {
  @Override
  default void formatTo(Formatter formatter, int flags, int width, int precision) {
    formatter.format("data");
  }
}
