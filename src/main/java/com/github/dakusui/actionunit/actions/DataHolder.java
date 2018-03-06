package com.github.dakusui.actionunit.actions;

import java.util.NoSuchElementException;
import java.util.function.Supplier;

/**
 * Differences from {@code Optional};
 * <ul>
 * <li>It can hold {@code null}, because it needs to be able to handle it.</li>
 * <li>Compiler doesn't complain of its use in parameters.</li>
 * <li>Also it doesn't complain of calling {@code get()} without checking {@code isPresent()} in advance.</li>
 * </ul>
 *
 * @param <T> Type of the value
 */
@FunctionalInterface
public interface DataHolder<T> extends Supplier<T> {
  T get();

  default boolean isPresent() {
    return false;
  }

  static <T> DataHolder<T> of(T data) {
    return new DataHolder<T>() {
      @Override
      public T get() {
        return data;
      }

      @Override
      public boolean isPresent() {
        return true;
      }
    };
  }

  static <T> DataHolder<T> empty() {
    return () -> {
      throw new NoSuchElementException();
    };
  }
}
