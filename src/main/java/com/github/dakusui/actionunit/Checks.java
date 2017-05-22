package com.github.dakusui.actionunit;

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
    if (!cond)
      throw new IllegalArgumentException(String.format(message, args));
  }

  public static void checkArgument(boolean cond) {
    checkArgument(cond, null);
  }

  public static <T extends RuntimeException> T propagate(Throwable t) {
    if (t instanceof Error)
      throw (Error) t;
    if (t instanceof RuntimeException)
      throw (RuntimeException) t;
    throw new RuntimeException(t);
  }
}
