package com.github.dakusui.actionunit.utils;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.lang.String.format;

public enum Checks {
  ;

  public static <T> T requireArgument(Predicate<T> condition, T value) {
    return requireArgument(condition, value, c -> v -> format("'%s' did not not satisfy '%s'", v, c));
  }

  public static <T> T requireArgument(Predicate<T> condition, T value, Function<Predicate<T>, Function<T, String>> messageComposer) {
    if (condition.test(value))
      return value;
    throw exceptionForIllegalValue(messageComposer.apply(condition).apply(value));
  }

  private static RuntimeException exceptionForIllegalValue(String message) {
    throw new IllegalArgumentException(message);
  }

  public static RuntimeException impossibleLineReached(String message, Throwable t) {
    throw exceptionForImpossibleLine(message, t);
  }

  private static RuntimeException exceptionForImpossibleLine(String message, Throwable t) {
    throw new AssertionError(message, t);
  }

  public static <T> T checkNotNull(T value, String message) {
    return Objects.requireNonNull(value, message);
  }

  public static <T> T checkNotNull(T value) {
    return Objects.requireNonNull(value);
  }

  public static void checkArgument(boolean cond, String message, Object... args) {
    checkArgument(cond, format(message, args));
  }

  public static void checkArgument(boolean cond, String message) {
    if (!cond)
      throw new IllegalArgumentException(message);
  }

  public static void checkArgument(boolean cond) {
    checkArgument(cond, null);
  }
}
