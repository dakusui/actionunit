package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface When2<T> extends Action {
  Supplier<T> value();

  Predicate<T> check();

  Action perform(Supplier<T> value);

  Action otherwise(Supplier<T> value);

  class Builder<T> {
    private final Supplier<T>       value;
    private final Predicate<T>      condition;
    private       HandlerFactory<T> handlerFactoryForPerform;
    private       HandlerFactory<T> handlerFactoryForOtherwise;

    public Builder(Supplier<T> value, Predicate<T> condition) {
      this.value = Objects.requireNonNull(value);
      this.condition = Objects.requireNonNull(condition);
    }

    public Builder<T> perform(HandlerFactory<T> factory) {
      this.handlerFactoryForPerform = Objects.requireNonNull(factory);
      return this;
    }

    public When2<T> otherwise(HandlerFactory<T> factory) {
      this.handlerFactoryForOtherwise = Objects.requireNonNull(factory);
      return build();
    }

    public When2<T> build() {
      return new When2.Impl<T>(
          value,
          condition,
          handlerFactoryForPerform,
          handlerFactoryForOtherwise
      );
    }
  }

  class Impl<T> extends ActionBase implements When2<T> {
    final private Supplier<T>       value;
    final private Predicate<T>      condition;
    final private HandlerFactory<T> handlerFactoryForPerform;
    final private HandlerFactory<T> handlerFactoryForOtherwise;

    public Impl(
        Supplier<T> value,
        Predicate<T> condition,
        HandlerFactory<T> handlerFactoryForPerform,
        HandlerFactory<T> handlerFactoryForOtherwise
    ) {
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
      return handlerFactoryForPerform.apply(Objects.requireNonNull(value));
    }

    @Override
    public Action otherwise(Supplier<T> value) {
      return handlerFactoryForOtherwise.apply(Objects.requireNonNull(value));
    }

    @Override
    public String toString() {
      return "When:";
    }
  }
}
