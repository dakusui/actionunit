package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.ActionFactory;
import com.github.dakusui.actionunit.core.Context;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface While<T> extends Action, Context {
  Supplier<T> value();

  Predicate<T> check();

  Action createAction();

  class Builder<T> {
    private final Predicate<T> check;
    private final Supplier<T>  value;
    private final int          id;

    public Builder(int id, Supplier<T> value, Predicate<T> check) {
      this.id = id;
      this.value = Objects.requireNonNull(value);
      this.check = Objects.requireNonNull(check);
    }

    public While<T> perform(ActionFactory actionFactory) {
      return new Impl<>(id, value, check, Objects.requireNonNull(actionFactory));
    }

    public While<T> perform(Action action) {
      return perform(self -> action);
    }
  }

  class Impl<T> extends ActionBase implements While<T> {
    private final Supplier<T>   value;
    private final Predicate<T>  check;
    private final ActionFactory actionFactory;

    Impl(int id, Supplier<T> value, Predicate<T> check, ActionFactory actionFactory) {
      super(id);
      this.value = value;
      this.check = check;
      this.actionFactory = actionFactory;
    }

    @Override
    public Supplier<T> value() {
      return value;
    }

    @Override
    public Predicate<T> check() {
      return check;
    }

    @Override
    public Action createAction() {
      return actionFactory.get();
    }

    @Override
    public void accept(Visitor visitor) {
      visitor.visit(this);
    }
  }
}
