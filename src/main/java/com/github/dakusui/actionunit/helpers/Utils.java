package com.github.dakusui.actionunit.helpers;

import java.util.function.Supplier;

/**
 * A utility class for static methods which are too trivial to create classes to which they should
 * belong.
 */
public enum Utils {
  ;

  public static <T> Supplier<T> toSupplier(T value) {
    return () -> value;
  }

  public static String spaces(int numSpaces) {
    StringBuilder ret = new StringBuilder();
    for (int i = 0; i < numSpaces; i++) {
      ret.append(" ");
    }
    return ret.toString();
  }
}
