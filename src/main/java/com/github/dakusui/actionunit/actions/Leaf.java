package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;

import java.util.Formatter;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.github.dakusui.actionunit.utils.InternalUtils.toStringIfOverriddenOrNoname;
import static java.util.Objects.requireNonNull;

public interface Leaf extends Action, Function<Context, Runnable> {
  Runnable runnable(Context context);

  default Runnable apply(Context context) {
    return runnable(context);
  }

  default void accept(Visitor visitor) {
    visitor.visit(this);
  }

  static Leaf of(Consumer<Context> consumer) {
    requireNonNull(consumer);
    return new Leaf() {
      @Override
      public Runnable runnable(Context context) {
        return () -> consumer.accept(context);
      }

      @Override
      public void formatTo(Formatter formatter, int flags, int width, int precision) {
        formatter.format("%s", toStringIfOverriddenOrNoname(consumer));
      }

      @Override
      public String toString() {
        return String.format("%s", this);
      }
    };
  }
}
