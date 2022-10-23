package com.github.dakusui.printables;

import com.github.dakusui.actionunit.utils.InternalUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.dakusui.actionunit.utils.InternalUtils.summary;
import static com.github.dakusui.actionunit.utils.InternalUtils.toStringIfOverriddenOrNoname;

public enum PrintableFunctionals {
  ;

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

  public static <I, O> Function<I, O> memoize(Function<I, O> function) {
    return printableFunction(new Function<I, O>() {
      final Map<I, O> cache = new ConcurrentHashMap<>();

      @Override
      public O apply(I i) {
        return cache.computeIfAbsent(i, function);
      }
    }).describe(InternalUtils.toStringIfOverriddenOrNoname(function));
  }

  public static <T> Consumer<T> functionToConsumer(Function<T, ?> function) {
    return printableConsumer(function::apply)
        .describe(toStringIfOverriddenOrNoname(function));
  }
}
