package com.github.dakusui.actionunit.generators;

import com.github.dakusui.actionunit.actions.ValueHolder;
import com.github.dakusui.actionunit.core.Context;

import java.util.function.Function;

public interface Generator<I, O> extends Function<ValueHolder<I>, Function<Context, O>> {
  default O apply(ValueHolder<I> valueHolder, Context context) {
    return this.apply(valueHolder).apply(context);
  }

  default O get(ValueHolder<I> valueHolder, Context context) {
    return apply(valueHolder, context);
  }
}
