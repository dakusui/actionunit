package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.github.dakusui.actionunit.core.ActionSupport.named;

public interface When<T> extends Action {
  Supplier<T> value();

  Predicate<T> check();

  Action perform(Supplier<T> value);

  Action otherwise(Supplier<T> value);

  class Builder<T> {
    private final Supplier<T>       value;
    private final Predicate<T>      condition;
    private final int               id;
    private       HandlerFactory<T> handlerFactoryForPerform;
    private HandlerFactory<T> handlerFactoryForOtherwise = new HandlerFactory.Base<T>() {
      @Override
      protected Action create(Supplier<T> data) {
        return nop();
      }
    };

    public Builder(int id, Supplier<T> value, Predicate<T> condition) {
      this.id = id;
      this.value = Objects.requireNonNull(value);
      this.condition = Objects.requireNonNull(condition);
    }

    public Builder<T> perform(HandlerFactory<T> factory) {
      this.handlerFactoryForPerform = Objects.requireNonNull(factory);
      return this;
    }

    public When<T> otherwise(HandlerFactory<T> factory) {
      this.handlerFactoryForOtherwise = Objects.requireNonNull(factory);
      return $();
    }

    public When<T> $() {
      return new When.Impl<T>(
          id,
          value,
          condition,
          handlerFactoryForPerform,
          handlerFactoryForOtherwise
      );
    }
  }

  class Impl<T> extends ActionBase implements When<T> {
    final private Supplier<T>       value;
    final private Predicate<T>      condition;
    final private HandlerFactory<T> handlerFactoryForPerform;
    final private HandlerFactory<T> handlerFactoryForOtherwise;

    public Impl(
        int id,
        Supplier<T> value,
        Predicate<T> condition,
        HandlerFactory<T> handlerFactoryForPerform,
        HandlerFactory<T> handlerFactoryForOtherwise
    ) {
      super(id);
      this.value = Objects.requireNonNull(value);
      this.condition = Objects.requireNonNull(condition);
      this.handlerFactoryForPerform = Objects.requireNonNull(handlerFactoryForPerform);
      this.handlerFactoryForOtherwise = Objects.requireNonNull(handlerFactoryForOtherwise);
    }

    @Override
    public void accept(Visitor visitor) {
      visitor.visit(this);
    }

    @Override
    public Supplier<T> value() {
      return value;
    }

    @Override
    public Predicate<T> check() {
      return condition;
    }

    @Override
    public Action perform(Supplier<T> value) {
      return named("perform", handlerFactoryForPerform.apply(Objects.requireNonNull(value)));
    }

    @Override
    public Action otherwise(Supplier<T> value) {
      return named("otherwise", handlerFactoryForOtherwise.apply(Objects.requireNonNull(value)));
    }
  }
}
