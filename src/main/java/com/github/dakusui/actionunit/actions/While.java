package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.generators.ActionGenerator;

import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public interface While<T> extends Action {
  Supplier<T> value();

  Predicate<T> check();

  Action createAction();

  class Builder<T> {
    private final Predicate<T> check;
    private final Supplier<T>  value;
    private final int          id;

    public Builder(int id, Supplier<T> value, Predicate<T> check) {
      this.id = id;
      this.value = requireNonNull(value);
      this.check = requireNonNull(check);
    }

    public While<T> perform(ActionGenerator<T> actionGenerator) {
      return new Impl<>(id, value, check, actionGenerator);
    }
  }

  class Impl<T> extends ActionBase implements While<T> {
    private final Supplier<T>        value;
    private final Predicate<T>       check;
    private final ActionGenerator<T> actionGenerator;

    Impl(int id, Supplier<T> value, Predicate<T> check, ActionGenerator<T> actionGenerator) {
      super(id);
      this.value = value;
      this.check = check;
      this.actionGenerator = requireNonNull(actionGenerator);
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
      return this.actionGenerator.<T>apply(ValueHolder.from(this.value)).apply(Context.create());
    }

    @Override
    public void accept(Visitor visitor) {
      visitor.visit(this);
    }
  }
}
