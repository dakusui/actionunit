package com.github.dakusui.actionunit.n.actions;

import com.github.dakusui.actionunit.n.core.Action;
import com.github.dakusui.actionunit.n.core.Context;
import com.github.dakusui.actionunit.n.core.ContextConsumer;

import java.util.Formatter;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public interface Leaf extends Action, Function<Context, Runnable> {
  Action NOP = Leaf.of(new ContextConsumer() {
    @Override
    public void accept(Context context) {
    }

    @Override
    public void formatTo(Formatter formatter, int flags, int width, int precision) {
      formatter.format("(nop)");
    }
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
