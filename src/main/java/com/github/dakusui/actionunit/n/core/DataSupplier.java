package com.github.dakusui.actionunit.n.core;

import java.util.Formattable;
import java.util.Formatter;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface DataSupplier<T> extends Supplier<Stream<T>>, Formattable {
  @Override
  default void formatTo(Formatter formatter, int flags, int width, int precision) {
    formatter.format("data");
  }
}
