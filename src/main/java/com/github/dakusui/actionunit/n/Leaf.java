package com.github.dakusui.actionunit.n;

import java.util.function.Function;

@FunctionalInterface
public interface Leaf extends Action, Function<Context, Runnable> {
  Runnable runnable(Context context);

  default Runnable apply(Context context) {
    return runnable(context);
  }

  default void accept(Visitor visitor) {
    visitor.visit(this);
  }

  static com.github.dakusui.actionunit.n.Leaf create(Function<Context, Runnable> func) {
    return func::apply;
  }

  static com.github.dakusui.actionunit.n.Leaf of(Runnable runnable) {
    return create(c -> runnable);
  }
}
