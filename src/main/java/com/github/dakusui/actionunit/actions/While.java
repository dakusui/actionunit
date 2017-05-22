package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.Action;

import java.util.function.Predicate;

import static com.github.dakusui.actionunit.Utils.describe;

public interface While extends Conditioned {
  class Impl extends Conditioned.Base implements While {
    public Impl(Predicate condition, Action action) {
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
