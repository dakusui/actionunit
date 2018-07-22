package com.github.dakusui.actionunit.generators;

import com.github.dakusui.actionunit.actions.ValueHolder;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;

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
