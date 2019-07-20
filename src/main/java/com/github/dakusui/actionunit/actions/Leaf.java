package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.context.ContextConsumer;

import java.util.Formatter;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public interface Leaf extends Action<Leaf>, Function<Context, Runnable> {
  Runnable runnable(Context context);

  default Runnable apply(Context context) {
    return runnable(context);
  }

  default void accept(Visitor visitor) {
    visitor.visit(this);
  }

  static Leaf of(ContextConsumer consumer) {
    requireNonNull(consumer);
    return new Leaf() {
      @Override
      public Runnable runnable(Context context) {
        return () -> consumer.accept(context);
      }

      @Override
      public void formatTo(Formatter formatter, int flags, int width, int precision) {
        formatter.format("%s", consumer);
      }
    };
  }
}
