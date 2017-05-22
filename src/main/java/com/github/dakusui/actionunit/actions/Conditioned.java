package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.Action;

import java.util.function.Predicate;

import static com.github.dakusui.actionunit.Checks.checkNotNull;

public interface Conditioned extends Nested, Predicate {
  abstract class Base extends Nested.Base implements Conditioned {
    private final Predicate condition;

    public Base(Predicate condition, Action action) {
      super(action);
      this.condition = checkNotNull(condition);
    }

    @Override
    public boolean test(Object input) {
      //noinspection unchecked
      return this.condition.test(input);
    }

    public Predicate getCondition() {
      return condition;
    }
  }
}
