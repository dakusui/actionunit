package com.github.dakusui.actionunit.generators;

import java.util.Objects;

public interface BooleanGenerator<I> extends ValueGenerator<I, Boolean> {
  static <I, V> BooleanGenerator<I> equalTo(ValueGenerator<I, V> value) {
    return iValueHolder -> context -> Objects.equals(value.apply(iValueHolder, context), iValueHolder.get());
  }
}
