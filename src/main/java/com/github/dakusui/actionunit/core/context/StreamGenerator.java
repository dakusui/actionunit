package com.github.dakusui.actionunit.core.context;

import com.github.dakusui.actionunit.core.Context;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

public interface StreamGenerator<T> extends ContextFunction<Stream<T>> {

  @SafeVarargs
  static <T> StreamGenerator<T> fromArray(T... elements) {
    return fromCollection(asList(elements));
  }

  static <T> StreamGenerator<T> fromCollection(Collection<T> collection) {
    requireNonNull(collection);
    return fromContextWith(new Function<Params, Stream<T>>() {
      @Override
      public Stream<T> apply(Params params) {
        return collection.stream();
      }

      public String toString() {
        return String.format("%s.stream()", collection.toString());
      }
    });
  }

  static <T> StreamGenerator<T> fromContextWith(Function<Params, Stream<T>> func, String... variableNames) {
    requireNonNull(func);
    return new StreamGenerator<T>() {
      @Override
      public Stream<T> apply(Context context) {
        return func.apply(Params.create(context, variableNames));
      }

      @Override
      public String toString() {
        return String.format("(%s)->%s",
            String.join(",", variableNames),
            MessageFormat.format(func.toString(), (Object[]) variableNames));
      }
    };
  }
}
