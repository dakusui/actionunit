package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.helpers.Actions;

import java.util.Objects;
import java.util.function.Predicate;

import static com.github.dakusui.actionunit.helpers.Utils.describe;
import static com.github.dakusui.actionunit.helpers.Checks.checkNotNull;

public interface When extends Action, Conditioned {
  Action otherwise();

  class Builder<T> {
    Predicate condition;
    Action    action;
    Action otherwise = Actions.nop();

    public Builder(Predicate<T> condition) {
      this.condition = Objects.requireNonNull(condition);
    }

    public Builder perform(Action action) {
      this.action = action;
      return this;
    }

    public Builder otherwise(Action action) {
      this.otherwise = action;
      return this;
    }

    /**
     * Builds an instance of  {@code When}.
     */
    public When build() {
      return new When.Impl(this.condition, this.action, this.otherwise);
    }
  }

  class Impl extends Conditioned.Base implements When {
    private final Action otherwise;

    public Impl(Predicate condition, Action action, Action otherwise) {
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
      return "When (" + describe(this.getCondition()) + ")";
    }
  }
}
