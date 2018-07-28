package com.github.dakusui.actionunit.compat.generators;

import com.github.dakusui.actionunit.compat.core.Context;

public interface NopGenerator<I> extends ActionGenerator<I> {
  NopGenerator<?> INSTANCE = (NopGenerator<Object>) objectValueHolder -> Context::nop;

  @SuppressWarnings("unchecked")
  static <I> NopGenerator<I> instance() {
    return (NopGenerator<I>) INSTANCE;
  }
}
