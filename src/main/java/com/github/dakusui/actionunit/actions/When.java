package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.ActionFactory;
import com.github.dakusui.actionunit.core.Context;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface When<T> extends Action, Context {
  Supplier<T> value();

  Predicate<T> check();

  Action perform();

  Action otherwise();

  class Builder<T> {
    private final Supplier<T>   value;
    private final Predicate<T>  condition;
    private final int           id;
    private       ActionFactory actionFactoryForPerform;
    private       ActionFactory actionFactoryForOtherwise;

    public Builder(int id, Supplier<T> value, Predicate<T> condition) {
      this.id = id;
      this.value = Objects.requireNonNull(value);
      this.condition = Objects.requireNonNull(condition);
    }

    public Builder<T> perform(ActionFactory factory) {
      this.actionFactoryForPerform = Objects.requireNonNull(factory);
      return this;
    }

    public Builder<T> perform(Action action) {
      return perform(self -> action);
    }

    public When<T> otherwise(ActionFactory factory) {
      this.actionFactoryForOtherwise = Objects.requireNonNull(factory);
      return build();
    }

    public When<T> otherwise(Action action) {
      return otherwise(self -> action);
    }

    public When<T> build() {
      return new When.Impl<>(
          id,
          value,
          condition,
          actionFactoryForPerform,
          actionFactoryForOtherwise != null
              ? actionFactoryForOtherwise
              : (ActionFactory) Context::nop
      );
    }
  }

  class Impl<T> extends ActionBase implements When<T> {
    final private Supplier<T>   value;
    final private Predicate<T>  condition;
    final private ActionFactory actionFactoryForPerform;
    final private ActionFactory actionFactoryForOtherwise;

    public Impl(
        int id,
        Supplier<T> value,
        Predicate<T> condition,
        ActionFactory actionFactoryForPerform,
        ActionFactory actionFactoryForOtherwise
    ) {
      super(id);
      this.value = Objects.requireNonNull(value);
      this.condition = Objects.requireNonNull(condition);
      this.actionFactoryForPerform = Objects.requireNonNull(actionFactoryForPerform);
      this.actionFactoryForOtherwise = Objects.requireNonNull(actionFactoryForOtherwise);
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
    public Action perform() {
      return Context.Internal.named(0, "perform", actionFactoryForPerform.get());
    }

    @Override
    public Action otherwise() {
      return Context.Internal.named(1, "otherwise", actionFactoryForOtherwise.get());
    }
  }
}
