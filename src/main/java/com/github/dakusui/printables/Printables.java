package com.github.dakusui.printables;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.dakusui.actionunit.utils.InternalUtils.summary;

public enum Printables {
  ;

  public static Predicate<String> isEmptyString() {
    return printablePredicate(String::isEmpty).describe("isEmptyString");
  }

  public static <T> PrintablePredicate.Builder<T> printablePredicate(Predicate<T> predicate) {
    return new PrintablePredicate.Builder<>(predicate);
  }

  public static <T> PrintableConsumer.Builder<T> printableConsumer(Consumer<T> consumer) {
    return new PrintableConsumer.Builder<>(consumer);
  }

  public static <T, R> PrintableFunction.Builder<T, R> printableFunction(Function<T, R> function) {
    return new PrintableFunction.Builder<>(function);
  }

  public static <T> Predicate<T> isKeyOf(Map<T, Object> values) {
    return printablePredicate((Predicate<T>) values::containsKey)
        .describe(() -> String.format("isKeyOf[%s]", summary(values.toString())));
  }

  public static <T> Predicate<T> isEqualTo(T value) {
    return printablePredicate((Predicate<T>) v -> Objects.equals(v, value))
        .describe(() -> String.format("is[%s]", value));
  }
}
