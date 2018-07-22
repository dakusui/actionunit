package com.github.dakusui.actionunit.generators;

import com.github.dakusui.actionunit.actions.ValueHolder;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public interface WhenGenerator<I> extends ActionGenerator<I> {
  static <I> WhenGenerator<I> create(BooleanGenerator<I> cond) {
    return new Impl<>(cond);
  }

  WhenGenerator<I> perform(ActionGenerator<I> perform);

  WhenGenerator<I> otherwise(ActionGenerator<I> otherwise);

  class Impl<I> implements WhenGenerator<I> {
    private final BooleanGenerator<I> cond;
    private       ActionGenerator<I>  otherwise = NopGenerator.instance();
    private       ActionGenerator<I>  perform = NopGenerator.instance();

    public Impl(BooleanGenerator<I> cond) {
      this.cond = requireNonNull(cond);
    }

    @Override
    public WhenGenerator<I> perform(ActionGenerator<I> perform) {
      this.perform = requireNonNull(perform);
      return this;
    }

    @Override
    public WhenGenerator<I> otherwise(ActionGenerator<I> otherwise) {
      this.otherwise = requireNonNull(otherwise);
      return this;
    }

    @Override
    public Function<Context, Action> apply(ValueHolder<I> valueHolder) {
      return context -> context.when(
          () -> cond.apply(valueHolder, context)
      ).perform(
          perform
      ).otherwise(
          otherwise
      );
    }
  }
}
