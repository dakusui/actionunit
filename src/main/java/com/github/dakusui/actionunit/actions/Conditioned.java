package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;

import java.util.function.Predicate;

import static com.github.dakusui.actionunit.helpers.Checks.checkNotNull;

public interface Conditioned<T> extends Nested, Predicate<T> {
  abstract class Base<T> extends Nested.Base implements Conditioned<T> {
    private final Predicate<T> condition;

    public Base(Predicate<T> condition, Action action) {
      super(action);
      this.condition = checkNotNull(condition);
    }

    @Override
    public boolean test(T input) {
      //noinspection unchecked
      return this.condition.test(input);
    }

    public Predicate<T> getCondition() {
      return condition;
    }
  }
}
