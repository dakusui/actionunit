package com.github.dakusui.actionunit.utils;

import java.util.function.Predicate;

public interface MatcherPredicate<T> extends Predicate<T>, Named {
  default String format(String varName) {
    return String.format("%s(%s)", name(), varName);
  }
}
