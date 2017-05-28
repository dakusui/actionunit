package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.helpers.Actions;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface While2<T> extends Action {
  Supplier<T> value();

  Predicate<T> check();

  Action createHandler(Supplier<T> data);

  class Builder<T> {
    private final Predicate<T> check;
    private final Supplier<T>  value;
    private HandlerFactory<T> handlerFactory = tSupplier -> Actions.nop();

    public Builder(Supplier<T> value, Predicate<T> check) {
      this.value = Objects.requireNonNull(value);
      this.check = Objects.requireNonNull(check);
    }

    public Builder<T> perform(HandlerFactory<T> handlerFactory) {
      this.handlerFactory = Objects.requireNonNull(handlerFactory);
      return this;
    }

    public While2<T> $() {
      return new While2<T>() {
        @Override
        public Supplier<T> value() {
          return value;
        }

        @Override
        public Predicate<T> check() {
          return check;
        }

        @Override
        public Action createHandler(Supplier<T> data) {
          return handlerFactory.apply(data);
        }

        @Override
        public void accept(Visitor visitor) {
          visitor.visit(this);
        }
      };
    }
  }
}
