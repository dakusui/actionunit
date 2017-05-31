package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.helpers.ActionSupport;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface While<T> extends Action {
  Supplier<T> value();

  Predicate<T> check();

  Action createHandler(Supplier<T> data);

  class Builder<T> {
    private final Predicate<T> check;
    private final Supplier<T>  value;
    private HandlerFactory<T> handlerFactory = tSupplier -> ActionSupport.nop();

    public Builder(Supplier<T> value, Predicate<T> check) {
      this.value = Objects.requireNonNull(value);
      this.check = Objects.requireNonNull(check);
    }

    public Builder<T> perform(HandlerFactory<T> handlerFactory) {
      this.handlerFactory = Objects.requireNonNull(handlerFactory);
      return this;
    }

    public While<T> $() {
      return new Impl<T>(value, check, handlerFactory);
    }
  }

  class Impl<T> extends ActionBase implements While<T> {
    private final Supplier<T>       value;
    private final Predicate<T>      check;
    private final HandlerFactory<T> handlerFactory;

    Impl(Supplier<T> value, Predicate<T> check, HandlerFactory<T> handlerFactory) {
      this.value = value;
      this.check = check;
      this.handlerFactory = handlerFactory;
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
    public Action createHandler(Supplier<T> data) {
      return handlerFactory.apply(data);
    }

    @Override
    public void accept(Visitor visitor) {
      visitor.visit(this);
    }
  }
}
