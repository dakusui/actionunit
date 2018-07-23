package com.github.dakusui.actionunit.generators;

import com.github.dakusui.actionunit.actions.ValueHolder;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.cmd.exceptions.UnexpectedExitValueException;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public interface CommanderGenerator<I> extends ActionGenerator<I> {
  static <I> CommanderGenerator<I> create(StringGenerator<I> program) {
    return new Impl<>(program);
  }

  CommanderGenerator<I> stdin(StreamGenerator<I, String> stdin);

  CommanderGenerator<I> retries(int times);

  CommanderGenerator<I> retryOn(Class<? extends Throwable> retryOn);

  CommanderGenerator<I> interval(long duration, TimeUnit timeUnit);

  CommanderGenerator<I> timeoutIn(StreamGenerator<I, String> stdin);

  class Impl<I> implements CommanderGenerator<I> {
    private final StringGenerator<I>         program;
    private       StreamGenerator<I, String> stdin = valueHolder -> context -> null;
    private int                              numRetries;
    private Class<? extends Throwable>       retryOn = UnexpectedExitValueException.class;
    private long                             retryIntervalDuration;
    private TimeUnit                         retryIntervalTimeUnit;

    protected Impl(StringGenerator<I> program) {
      this.program = requireNonNull(program);
    }

    @Override
    public Function<Context, Action> apply(ValueHolder<I> valueHolder) {
      return new Function<Context, Action>() {
        @Override
        public Action apply(Context context) {
          return context.cmd(
              program.get(valueHolder, context)
          ).stdin(
              stdin.get(valueHolder, context)
          ).build();
        }
      };
    }

    @Override
    public CommanderGenerator<I> stdin(StreamGenerator<I, String> stdin) {
      this.stdin = requireNonNull(stdin);
      return this;
    }

    @Override
    public CommanderGenerator<I> retries(int times) {
      this.numRetries = times;
      return this;
    }

    @Override
    public CommanderGenerator<I> retryOn(Class<? extends Throwable> retryOn) {
      this.retryOn = requireNonNull(retryOn);
      return this;
    }

    @Override
    public CommanderGenerator<I> interval(long duration, TimeUnit timeUnit) {
      this.retryIntervalDuration = duration;
      this.retryIntervalTimeUnit = requireNonNull(timeUnit);
      return this;
    }

    @Override
    public CommanderGenerator<I> timeoutIn(StreamGenerator<I, String> stdin) {
      this.stdin = requireNonNull(stdin);
      return this;
    }
  }
}
