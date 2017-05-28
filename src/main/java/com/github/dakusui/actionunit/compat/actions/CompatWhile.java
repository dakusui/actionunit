package com.github.dakusui.actionunit.compat.actions;

import com.github.dakusui.actionunit.actions.Conditioned;
import com.github.dakusui.actionunit.core.Action;

import java.util.Objects;
import java.util.function.Predicate;

import static com.github.dakusui.actionunit.helpers.Utils.describe;

public interface CompatWhile<T> extends Conditioned<T> {
  class Builder<T> {
    Predicate<T> condition;
    Action       action;

    public Builder(Predicate<T> condition) {
      this.condition = Objects.requireNonNull(condition);
    }

    public CompatWhile<T> perform(Action action) {
      return new Impl<>(this.condition, Objects.requireNonNull(action));
    }
  }

  class Impl<T> extends Conditioned.Base<T> implements CompatWhile<T> {
    public Impl(Predicate<T> condition, Action action) {
      super(condition, action);
    }

    @Override
    public void accept(Visitor visitor) {
      visitor.visit(this);
    }

    @Override
    public String toString() {
      return "CompatWhile (" + describe(this.getCondition()) + ")";
    }
  }
}
