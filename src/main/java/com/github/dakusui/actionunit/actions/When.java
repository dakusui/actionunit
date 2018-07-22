package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.generators.ActionGenerator;

import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public interface When<T> extends Action {
  Supplier<T> value();

  Predicate<T> check();

  Action perform();

  Action otherwise();

  class Builder<T> {
    private final Supplier<T>        value;
    private final Predicate<T>       condition;
    private final int                id;
    private       ActionGenerator<?> actionGeneratorForPerform;
    private       ActionGenerator<?> actionGeneratorForOtherwise;

    public Builder(int id, Supplier<T> value, Predicate<T> condition) {
      this.id = id;
      this.value = requireNonNull(value);
      this.condition = requireNonNull(condition);
    }

    public Builder<T> perform(ActionGenerator<?> actionGeneratorForPerform) {
      this.actionGeneratorForPerform = requireNonNull(actionGeneratorForPerform);
      return this;
    }

    public When<T> otherwise(ActionGenerator<?> generator) {
      this.actionGeneratorForOtherwise = requireNonNull(generator);
      return build();
    }

    public When<T> build() {
      return new When.Impl<>(
          id,
          value,
          condition,
          actionGeneratorForPerform,
          actionGeneratorForOtherwise != null
              ? actionGeneratorForOtherwise
              : ActionGenerator.of(v -> Context::nop)
      );
    }
  }

  class Impl<T> extends ActionBase implements When<T> {
    final private Supplier<T>        value;
    final private Predicate<T>       condition;
    final private ActionGenerator<?> actionGeneratorForPerform;
    final private ActionGenerator<?> actionGeneratorForOtherwise;

    public Impl(
        int id,
        Supplier<T> value,
        Predicate<T> condition,
        ActionGenerator<?> actionGeneratorForPerform,
        ActionGenerator<?> actionGeneratorForOtherwise
    ) {
      super(id);
      this.value = requireNonNull(value);
      this.condition = requireNonNull(condition);
      this.actionGeneratorForPerform = requireNonNull(actionGeneratorForPerform);
      this.actionGeneratorForOtherwise = requireNonNull(actionGeneratorForOtherwise);
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
      return Context.Internal.named(
          0,
          "perform",
          actionGeneratorForPerform.apply(ValueHolder.empty(), Context.create())
      );
    }

    @Override
    public Action otherwise() {
      return Context.Internal.named(
          1,
          "otherwise",
          actionGeneratorForOtherwise.apply(ValueHolder.empty(), Context.create())
      );
    }
  }
}
