package com.github.dakusui.actionunit.generators;

import com.github.dakusui.actionunit.actions.ValueHolder;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public interface SimpleGenerator<I> extends ActionGenerator<I> {

  SimpleGenerator<I> describe(String description);

  class Impl<I> implements SimpleGenerator<I> {
    final   RunnableGenerator<I> runnableGenerator;
    private String               description = "(noname)";

    public Impl(RunnableGenerator<I> runnableGenerator) {
      this.runnableGenerator = requireNonNull(runnableGenerator);
    }

    @Override
    public Function<Context, Action> apply(ValueHolder<I> i) {
      return context -> context.simple(
          description,
          runnableGenerator.apply(i, context)
      );
    }

    @Override
    public SimpleGenerator<I> describe(String description) {
      this.description = description;
      return this;
    }
  }

  static <I> SimpleGenerator<I> create(RunnableGenerator<I> runnableGenerator) {
    return new Impl<>(runnableGenerator);
  }
}
