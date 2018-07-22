package com.github.dakusui.actionunit.generators;

import com.github.dakusui.actionunit.actions.ValueHolder;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.visitors.reporting.Report;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public interface AttemptGenerator<I> extends ActionGenerator<I> {
  default AttemptGenerator<I> recover(
      Class<? extends Throwable> exceptionClass,
      ActionGenerator<Throwable> recoverBy) {
    bean().recover(
        exceptionClass,
        recoverBy
    );
    return this;
  }

  default AttemptGenerator<I> ensure(ActionGenerator<I> ensured) {
    bean().ensure(ensured);
    return this;
  }

  Bean bean();

  class Bean {
    final ActionGenerator<?> target;
    Class<? extends Throwable>           exceptionClass;
    ActionGenerator<? extends Throwable> exceptionHandler;
    ActionGenerator<?>                   ensured;
    private ActionGenerator<Throwable> recoverBy;

    public Bean(ActionGenerator<?> target) {
      this.target = requireNonNull(target);
    }

    public void recover(
        Class<? extends Throwable> exceptionClass,
        ActionGenerator<Throwable> recoverBy) {
      this.exceptionClass = exceptionClass;
      this.recoverBy = recoverBy;
    }

    public void ensure(ActionGenerator<?> ensured) {
      this.ensured = requireNonNull(ensured);
    }
  }

  static <I> AttemptGenerator<I> create(ActionGenerator<I> target) {
    return new AttemptGenerator<I>() {

      private ActionGenerator<Throwable> exceptionHandlingGenerator = new ActionGenerator<Throwable>() {
        @Override
        public Function<Context, Action> apply(ValueHolder<Throwable> throwableValueHolder) {
          return context -> context.simple("noname", () -> {
            throw new RuntimeException(throwableValueHolder.get());
          });
        }
      };
      private Class<? extends Throwable> exceptionClass = Throwable.class;
      private ActionGenerator<?> ensuredActionGenerator = v -> Context::nop;

      @Override
      public Function<Context, Action> apply(ValueHolder<I> valueHolder) {
        return context -> context.attempt(
            target.get(valueHolder, context)
        ).recover(
            exceptionClass, exceptionHandlingGenerator
        ).ensure(
            ensuredActionGenerator
        );
      }

      Bean bean = new Bean(target);

      @Override
      public Bean bean() {
        return bean;
      }

    };
  }
}
