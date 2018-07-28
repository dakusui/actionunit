package com.github.dakusui.actionunit.n.core.context;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public interface Block extends Consumer<Context> {

  static <T> Block create(String variableName, Consumer<T> consumer) {
    requireNonNull(variableName);
    return context -> consumer.accept(context.valueOf(variableName));
  }

  static <T, U> Block create(String variableName1, String variableName2, BiConsumer<T, U> consumer) {
    requireNonNull(variableName1);
    requireNonNull(variableName2);
    return context -> consumer.accept(context.valueOf(variableName1), context.valueOf(variableName2));
  }
}
