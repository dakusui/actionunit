package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.Action;

import java.util.function.Predicate;

import static com.github.dakusui.actionunit.Utils.describe;
import static com.github.dakusui.actionunit.Checks.checkNotNull;

public interface When extends Action, Conditioned {
  Action otherwise();

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
