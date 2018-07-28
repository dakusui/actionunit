package com.github.dakusui.actionunit.compat.generators;

import com.github.dakusui.actionunit.compat.actions.ValueHolder;
import com.github.dakusui.actionunit.compat.core.Context;

import java.util.function.Function;

public interface RunnableGenerator<I> extends ValueGenerator<I, Runnable> {
  static <I> RunnableGenerator<I> of(Function<ValueHolder<I>, Function<Context, Runnable>> func) {
    return valueHolder -> context -> func.apply(valueHolder).apply(context);
  }

  static <I> RunnableGenerator<I> from(Runnable runnable) {
    return v -> c -> runnable;
  }
}
