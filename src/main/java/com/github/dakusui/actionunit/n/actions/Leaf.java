package com.github.dakusui.actionunit.n.actions;

import com.github.dakusui.actionunit.n.core.Action;
import com.github.dakusui.actionunit.n.core.context.Context;
import com.github.dakusui.actionunit.n.core.context.ContextConsumer;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

@FunctionalInterface
public interface Leaf extends Action, Function<Context, Runnable> {
  Action NOP = Leaf.of(context -> {
  });

  Runnable runnable(Context context);

  default Runnable apply(Context context) {
    return runnable(context);
  }

  default void accept(Visitor visitor) {
    visitor.visit(this);
  }

  static Leaf of(ContextConsumer consumer) {
    requireNonNull(consumer);
    return context -> () -> consumer.accept(context);
  }
}
