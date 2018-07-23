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
      ActionGenerator<Throwable> recoverBy);

  AttemptGenerator<I> ensure(ActionGenerator<I> ensured);


  class Impl<I> implements AttemptGenerator<I> {
    private final ActionGenerator<I>         target;
    private       Class<? extends Throwable> exceptionClass = Throwable.class;
    private       ActionGenerator<Throwable> recoverBy      = SimpleGenerator.create(
        RunnableGenerator.of(throwableValueHolder -> context -> (Runnable) () -> {
          Throwable t = throwableValueHolder.get();
          if (t instanceof Error)
            throw (Error) t;
          if (t instanceof RuntimeException)
            throw (RuntimeException) t;
          throw new ActionException(t);
        })
    );
    private       ActionGenerator<?>         ensured        = NopGenerator.instance();

    public Impl(ActionGenerator<I> target) {
      this.target = requireNonNull(target);
    }

    public AttemptGenerator<I> recover(
        Class<? extends Throwable> exceptionClass,
        ActionGenerator<Throwable> recoverBy) {
      this.exceptionClass = exceptionClass;
      this.recoverBy = recoverBy;
      return this;
    }

    public AttemptGenerator<I> ensure(ActionGenerator<I> ensured) {
      this.ensured = requireNonNull(ensured);
      return this;
    }

    @Override
    public Function<Context, Action> apply(ValueHolder<I> i) {
      return context -> context.attempt(target.apply(i, context)).recover(exceptionClass, recoverBy).ensure(ensured);
    }
  }


  static <I> AttemptGenerator<I> create(ActionGenerator<I> target) {
    return new Impl<>(target);
  }
}
