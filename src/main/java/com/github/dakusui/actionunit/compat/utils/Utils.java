package com.github.dakusui.actionunit.compat.utils;

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

}
