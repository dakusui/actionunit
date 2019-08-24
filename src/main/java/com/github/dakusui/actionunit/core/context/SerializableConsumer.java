package com.github.dakusui.actionunit.core.context;

import java.io.Serializable;
import java.util.function.Consumer;

@FunctionalInterface
public interface SerializableConsumer<T> extends Consumer<T>, Serializable {
  static <T> SerializableConsumer<T> of(Consumer<T> consumer) {
    return (SerializableConsumer<T>) consumer::accept;
  }
}
