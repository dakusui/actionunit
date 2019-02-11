package com.github.dakusui.actionunit.core;

import com.github.dakusui.actionunit.core.ContextFunctions.Params;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Formattable;
import java.util.Formatter;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.dakusui.actionunit.utils.InternalUtils.objectToStringIfOverridden;
import static java.util.Objects.requireNonNull;

public interface StreamGenerator<T> extends Function<Context, Stream<T>>, Formattable {
  @Override
  default void formatTo(Formatter formatter, int flags, int width, int precision) {
    formatter.format(objectToStringIfOverridden(this, "(data)"));
  }

  static <T> StreamGenerator<T> createFrom(Collection<T> collection) {
    requireNonNull(collection);
    return createFromContextWith(new Function<Params, Stream<T>>() {
      @Override
      public Stream<T> apply(Params params) {
        return collection.stream();
      }

      public String toString() {
        return String.format("%s.stream()", collection.toString());
      }
    });
  }

  static <T> StreamGenerator<T> createFromContextWith(Function<Params, Stream<T>> func, String... variableNames) {
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
