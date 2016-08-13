package com.github.dakusui.actionunit;

import java.util.Objects;

public interface Source<T> {
  T apply();

  class Fixed<T> implements Source<T>, Describable {
    private final T value;

    public Fixed(T value) {
      this.value = value;
    }

    public T apply() {
      return this.value;
    }

    @Override
    public String describe() {
      return Objects.toString(this.value);
    }
  }

  enum Factory {
    ;
    public static <V> Source<V> create(V value) {
      return new Fixed<V>(value);
    }
  }
}
