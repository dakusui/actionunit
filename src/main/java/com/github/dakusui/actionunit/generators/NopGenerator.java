package com.github.dakusui.actionunit.generators;

import com.github.dakusui.actionunit.core.Context;

public interface NopGenerator<I> extends ActionGenerator<I> {
  NopGenerator<?> INSTANCE = (NopGenerator<Object>) objectValueHolder -> Context::nop;

  @SuppressWarnings("unchecked")
  static <I> NopGenerator<I> instance() {
    return (NopGenerator<I>) INSTANCE;
  }
}
