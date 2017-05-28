package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.helpers.Actions;

import java.util.Objects;
import java.util.function.Predicate;

import static com.github.dakusui.actionunit.helpers.Utils.describe;
import static com.github.dakusui.actionunit.helpers.Checks.checkNotNull;

public interface CompatWhen<T> extends Action, Conditioned<T> {
  Action otherwise();

  class Builder<T> {
    Predicate<T> condition;
    Action       action;
    Action otherwise = Actions.nop();

    public Builder(Predicate<T> condition) {
      this.condition = Objects.requireNonNull(condition);
    }

    public Builder<T> perform(Action action) {
      this.action = action;
      return this;
    }

    public Builder<T> otherwise(Action action) {
      this.otherwise = action;
      return this;
    }

    /**
     * Builds an instance of {@code CompatWhen}.
     */
    public CompatWhen<T> $() {
      return new CompatWhen.Impl<>(this.condition, this.action, this.otherwise);
    }
  }

  class Impl<T> extends Conditioned.Base<T> implements CompatWhen<T> {
    private final Action otherwise;

    public Impl(Predicate<T> condition, Action action, Action otherwise) {
      super(condition, action);
      this.otherwise = checkNotNull(otherwise);
    }

    @Override
    public Action otherwise() {
      return this.otherwise;
    }

    @Override
    public void accept(Visitor visitor) {
      visitor.visit(this);
    }

    @Override
    public String toString() {
      return "CompatWhen (" + describe(this.getCondition()) + ")";
    }
  }
}
