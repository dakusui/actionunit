package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;

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
    private final int          id;
    private HandlerFactory<T> handlerFactory = new HandlerFactory.Base<T>() {
      @Override
      protected Action create(Supplier<T> data) {
        return nop();
      }
    };

    public Builder(int id, Supplier<T> value, Predicate<T> check) {
      this.id = id;
      this.value = Objects.requireNonNull(value);
      this.check = Objects.requireNonNull(check);
    }

    public While<T> perform(HandlerFactory<T> handlerFactory) {
      this.handlerFactory = Objects.requireNonNull(handlerFactory);
      return new Impl<T>(id, value, check, handlerFactory);
    }
  }

  class Impl<T> extends ActionBase implements While<T> {
    private final Supplier<T>       value;
    private final Predicate<T>      check;
    private final HandlerFactory<T> handlerFactory;

    Impl(int id, Supplier<T> value, Predicate<T> check, HandlerFactory<T> handlerFactory) {
      super(id);
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
