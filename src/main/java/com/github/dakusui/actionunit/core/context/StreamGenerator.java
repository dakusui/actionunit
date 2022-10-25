package com.github.dakusui.actionunit.core.context;

import com.github.dakusui.actionunit.actions.ContextVariable;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.context.multiparams.Params;
import com.github.dakusui.actionunit.utils.StableTemplatingUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

public interface StreamGenerator<T> extends ContextFunction<Stream<T>> {

  @SafeVarargs
  static <T> Function<Context, Stream<T>> fromArray(T... elements) {
    return fromCollection(asList(elements));
  }

  static <T> Function<Context, Stream<T>> fromCollection(Collection<T> collection) {
    requireNonNull(collection);
    return fromContextWith(new Function<Params, Stream<T>>() {
      @Override
      public Stream<T> apply(Params params) {
        return collection.stream();
      }

      public String toString() {
        return String.format("%s.stream()", collection);
      }
    });
  }

  static <T> Function<Context, Stream<T>> fromContextWith(Function<Params, Stream<T>> func, ContextVariable... variables) {
    requireNonNull(func);
    return new StreamGenerator<T>() {
      @Override
      public Stream<T> apply(Context context) {
        return func.apply(Params.create(context, variables));
      }

      @Override
      public String toString() {
        return String.format("(%s)->%s",
            String.join(",", Arrays.stream(variables)
                .map(ContextVariable::variableName)
                .toArray(String[]::new)),
            StableTemplatingUtils.template(
                func.toString(),
                Arrays.stream(variables)
                    .collect(toMap(
                        ContextVariable::variableName,
                        k -> String.format("{{%s}}", k)))));
      }
    };
  }
}
