package com.github.dakusui.actionunit.generators;

import com.github.dakusui.actionunit.actions.ValueHolder;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.exceptions.ActionException;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public interface AttemptGenerator<I> extends ActionGenerator<I> {
  AttemptGenerator<I> recover(
      Class<? extends Throwable> exceptionClass,
      ActionGenerator<Throwable> recoverBy
  );

  AttemptGenerator<I> ensure(ActionGenerator<I> ensured);

  class Impl<I> implements AttemptGenerator<I> {
    final ActionGenerator<I> target;
    Class<? extends Throwable>           exceptionClass   = Throwable.class;
    ActionGenerator<? extends Throwable> exceptionHandler = ActionGenerator.of(
        throwable -> context -> {
          Throwable t = throwable.get();
          if (t instanceof Error)
            throw (Error) t;
          if (t instanceof RuntimeException)
            throw (RuntimeException) t;
          throw new ActionException(t);
        }
    );
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
