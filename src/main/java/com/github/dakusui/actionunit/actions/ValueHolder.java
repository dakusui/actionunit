package com.github.dakusui.actionunit.actions;

import java.util.Formattable;
import java.util.Formatter;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

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
public interface ValueHolder<T> extends Supplier<T>, Formattable {
  T get();

  default boolean isPresent() {
    return false;
  }

  default T orElse(T value) {
    return isPresent() ?
        get() :
        value;
  }

  default void formatTo(Formatter formatter, int flags, int width, int precision) {
    formatter.format("%s", isPresent() ?
        get() :
        emptyString()
    );
  }

  default String emptyString() {
    return "[empty]";
  }

  static <T> ValueHolder<T> of(T data) {
    requireNonNull(data);
    return new ValueHolder<T>() {
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

  static <T> ValueHolder<T> empty() {
    return () -> {
      throw new NoSuchElementException();
    };
  }
}
