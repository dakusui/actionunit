package com.github.dakusui.printables;

import java.util.function.Consumer;
import java.util.function.Predicate;

public enum Printables {
  ;

  public static <T> PrintablePredicate.Builder<T> predicate(Predicate<T> predicate) {
    return new PrintablePredicate.Builder<>(predicate);
  }

  public static <T> PrintableConsumer.Builder<T> consumer(Consumer<T> consumer) {
    return new PrintableConsumer.Builder<>(consumer);
  }
}
