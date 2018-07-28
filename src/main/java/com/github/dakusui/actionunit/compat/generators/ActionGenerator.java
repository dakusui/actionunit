package com.github.dakusui.actionunit.compat.generators;

import com.github.dakusui.actionunit.compat.actions.ValueHolder;
import com.github.dakusui.actionunit.compat.core.Action;
import com.github.dakusui.actionunit.compat.core.Context;

import java.util.function.Function;

public interface ActionGenerator<I> extends Generator<I, Action> {
  static <I> ActionGenerator<I> of(Function<ValueHolder<I>, Function<Context, Action>> func) {
    return valueHolder -> context -> func.apply(valueHolder).apply(context);
  }

  static <I> ActionGenerator<I> from(Function<Context, Action> func) {
    return valueHolder -> func;
  }

  static <I> ActionGenerator<I> from(Action action) {
    return valueHolder -> context -> action;
  }
}
