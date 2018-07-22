package com.github.dakusui.actionunit.generators;

import com.github.dakusui.actionunit.actions.ValueHolder;
import com.github.dakusui.actionunit.core.Context;

import java.util.function.Function;
import java.util.function.Predicate;

public interface PredicateGenerator<I, T> extends ValueGenerator<T, Predicate<ValueGenerator<I, T>>> {
  static <I, T> PredicateGenerator<I, T> of(Predicate<T> predicate) {
    return new PredicateGenerator<I, T>() {
      @Override
      public Function<Context, Predicate<ValueGenerator<I, T>>> apply(ValueHolder<T> valueHolder) {
        return new Function<Context, Predicate<ValueGenerator<I, T>>>() {
          @Override
          public Predicate<ValueGenerator<I, T>> apply(Context context) {
            return new Predicate<ValueGenerator<I, T>>() {
              @Override
              public boolean test(ValueGenerator<I, T> itValueGenerator) {
                return predicate.test(valueHolder.get());
              }
            };

          }
        };
      }
    };
  }
}
