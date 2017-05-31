package com.github.dakusui.actionunit.helpers;

import com.github.dakusui.actionunit.actions.HandlerFactory;

import java.util.function.Consumer;
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

  public static <T> HandlerFactory<T> handlerFactory(String description, Consumer<T> handlerBody) {
    return HandlerFactory.create(description, handlerBody);
  }
}
