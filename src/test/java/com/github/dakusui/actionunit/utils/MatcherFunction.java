package com.github.dakusui.actionunit.utils;
import java.util.function.Function;

public interface MatcherFunction<I, O> extends Function<I, O>, Named {
  default String format(String varName) {
    return String.format("%s(%s)", name(), varName);
  }
}
