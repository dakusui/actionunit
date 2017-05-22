package com.github.dakusui.actionunit;

import com.github.dakusui.actionunit.visitors.ActionRunner;
import org.junit.Test;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class TestAction2Example {
  @Test
  public void given$when$then() {
    Actions.<String, Integer>test2()
        .given(new Function<Supplier<String>, Action>() {
          @Override
          public Action apply(Supplier<String> stringSupplier) {
            return null;
          }
        })
        .when(new Function<Function<String, Integer>, Action>() {
          @Override
          public Action apply(Function<String, Integer> stringIntegerFunction) {
            return null;
          }
        })
        .then(new Function<Predicate<Integer>, Action>() {
          @Override
          public Action apply(Predicate<Integer> integerPredicate) {
            return null;
          }
        })
        .build()
        .accept(new ActionRunner.Impl());
  }
}
