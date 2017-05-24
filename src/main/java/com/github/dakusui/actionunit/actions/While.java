package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;

import java.util.Objects;
import java.util.function.Predicate;

import static com.github.dakusui.actionunit.helpers.Utils.describe;

public interface While<T> extends Conditioned<T> {
  class Builder<T> {
    Predicate<T> condition;
    Action       action;

    public Builder(Predicate<T> condition) {
      this.condition = Objects.requireNonNull(condition);
    }

    public While<T> perform(Action action) {
      return new Impl<>(this.condition, Objects.requireNonNull(action));
    }
  }

  class Impl<T> extends Conditioned.Base<T> implements While<T> {
    public Impl(Predicate<T> condition, Action action) {
      super(condition, action);
    }

    @Override
    public void accept(Visitor visitor) {
      visitor.visit(this);
    }

    @Override
    public String toString() {
      return "While (" + describe(this.getCondition()) + ")";
    }
  }
}
