package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.Action;
import com.google.common.base.Predicate;

import static com.github.dakusui.actionunit.Utils.describe;
import static com.google.common.base.Preconditions.checkNotNull;

public interface Conditioned extends Nested, Predicate {
  abstract class Base extends Nested.Base implements Conditioned {
    private final Predicate condition;

    public Base(Predicate condition, Action action) {
      super(action);
      this.condition = checkNotNull(condition);
    }

    @Override
    public boolean apply(Object input) {
      //noinspection unchecked
      return this.condition.apply(input);
    }

    public Predicate getCondition() {
      return condition;
    }
  }
}
