package com.github.dakusui.actionunit.core;

import java.util.Formattable;
import java.util.Formatter;
import java.util.function.Predicate;

public interface ContextPredicate extends Predicate<Context>, Formattable {
  @Override
  default void formatTo(Formatter formatter, int flags, int width, int precision) {
    formatter.format("condition");
  }
}
