package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.Action;
import com.google.common.base.Predicate;

import static com.github.dakusui.actionunit.Utils.describe;
import static com.google.common.base.Preconditions.checkNotNull;

public interface When extends Action, Nested, Predicate {
  Action otherwise();

  class Impl extends Nested.Base implements When {
    private final Predicate condition;
    private final Action    otherwise;

    public Impl(Predicate condition, Action action, Action otherwise) {
      super(action);
      this.condition = checkNotNull(condition);
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
    public boolean apply(Object input) {
      //noinspection unchecked
      return this.condition.apply(input);
    }

    @Override
    public String toString() {
      return "When (" + describe(this.condition) + ")";
    }
  }
}
