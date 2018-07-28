package com.github.dakusui.actionunit.generators;

import com.github.dakusui.actionunit.actions.ValueHolder;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.n.exceptions.ActionException;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public interface AttemptGenerator<I> extends ActionGenerator<I> {

  ActionGenerator<Throwable> RETHROW_EXCEPTION = ActionGenerator.of(
      throwable -> (Function<Context, Action>) context ->
          context.simple("handle",
              () -> {
                throw ActionException.wrap(throwable.get());
              }
          )
  );

  AttemptGenerator<I> recover(
      Class<? extends Throwable> exceptionClass,
      ActionGenerator<Throwable> recoverBy
  );

  AttemptGenerator<I> ensure(ActionGenerator<I> ensured);

  class Impl<I> implements AttemptGenerator<I> {
    final ActionGenerator<I> target;
    Class<? extends Throwable>           exceptionClass   = Throwable.class;
    ActionGenerator<? extends Throwable> exceptionHandler = RETHROW_EXCEPTION;
    ActionGenerator<?>                   ensured;

    public Impl(ActionGenerator<I> target) {
      this.target = requireNonNull(target);
    }

    @Override
    public AttemptGenerator<I> recover(
        Class<? extends Throwable> exceptionClass,
        ActionGenerator<Throwable> recoverBy) {
      this.exceptionClass = requireNonNull(exceptionClass);
      this.exceptionHandler = requireNonNull(recoverBy);
      return this;
    }

    @Override
    public AttemptGenerator<I> ensure(ActionGenerator<I> ensured) {
      this.ensured = requireNonNull(ensured);
      return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Function<Context, Action> apply(ValueHolder<I> valueHolder) {
      return context -> context.attempt(
          Impl.this.target.apply(valueHolder, context)
      ).recover(
          Impl.this.exceptionClass,
          (ActionGenerator<Throwable>) Impl.this.exceptionHandler
      ).ensure(
          Impl.this.ensured
      );
    }
  }

  static <I> AttemptGenerator<I> create(ActionGenerator<I> target) {
    return new Impl<>(target);
  }
}
