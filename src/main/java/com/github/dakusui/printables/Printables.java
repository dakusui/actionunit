package com.github.dakusui.printables;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.github.dakusui.actionunit.utils.InternalUtils.summary;

public enum Printables {
  ;

  public static <T> PrintablePredicate.Builder<T> predicate(Predicate<T> predicate) {
    return new PrintablePredicate.Builder<>(predicate);
  }

  public static <T> PrintableConsumer.Builder<T> consumer(Consumer<T> consumer) {
    return new PrintableConsumer.Builder<T>(consumer);
  }

  public static <T> Predicate<T> isKeyOf(Map<T, Object> values) {
    return predicate((Predicate<T>) values::containsKey)
        .describe(() -> String.format("isKeyOf[%s]", summary(values.toString())));
  }
}
