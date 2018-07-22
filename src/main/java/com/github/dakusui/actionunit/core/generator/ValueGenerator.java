package com.github.dakusui.actionunit.core.generator;

import com.github.dakusui.actionunit.actions.ValueHolder;
import com.github.dakusui.actionunit.core.Context;

import java.util.function.Function;
import java.util.function.Supplier;

@FunctionalInterface
public interface ValueGenerator<I, O>
    extends Generator<I, O> {
  default O get(ValueHolder<I> valueHolder, Context context) {
    return this.apply(valueHolder).apply(context);
  }
}
