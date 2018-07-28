package com.github.dakusui.actionunit.compat.generators;

import com.github.dakusui.actionunit.compat.actions.ValueHolder;
import com.github.dakusui.actionunit.compat.core.Context;

import java.util.function.Function;

public interface Generator<I, O> extends Function<ValueHolder<I>, Function<Context, O>> {
  default O apply(ValueHolder<I> valueHolder, Context context) {
    return this.apply(valueHolder).apply(context);
  }

  default O get(ValueHolder<I> valueHolder, Context context) {
    return apply(valueHolder, context);
  }
}
