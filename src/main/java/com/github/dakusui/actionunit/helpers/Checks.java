package com.github.dakusui.actionunit.helpers;

import java.util.Objects;

public enum Checks {
  ;

  public static <T> T checkNotNull(T value, String message) {
    return Objects.requireNonNull(value, message);
  }

  public static <T> T checkNotNull(T value) {
    return Objects.requireNonNull(value);
  }

  public static void checkArgument(boolean cond, String message, Object... args) {
    checkArgument(cond, String.format(message, args));
  }

  public static void checkArgument(boolean cond, String message) {
    if (!cond)
      throw new IllegalArgumentException(message);
  }

  public static void checkArgument(boolean cond) {
    checkArgument(cond, null);
  }

  public static void checkState(boolean cond, String format, Object... args) {
    checkState(cond, String.format(format, args));
  }

  public static void checkState(boolean cond, String message) {
    if (!cond)
      throw new IllegalStateException(message);
  }

  public static UnsupportedOperationException notPrintable() {
    throw new UnsupportedOperationException("This action cannot be printed");
  }

  public static <T extends RuntimeException> T propagate(Throwable t) {
    if (t instanceof Error)
      throw (Error) t;
    if (t instanceof RuntimeException)
      throw (RuntimeException) t;
    throw new RuntimeException(t);
  }
}
