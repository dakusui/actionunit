package com.github.dakusui.actionunit.extras.cmd;

import com.github.dakusui.actionunit.generators.ValueGenerator;

public interface IntGenerator<I> extends ValueGenerator<I, Integer> {
  static <I> IntGenerator<I> create(int value) {
    return valueHolder -> context -> value;
  }
}
