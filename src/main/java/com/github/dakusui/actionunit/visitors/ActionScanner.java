package com.github.dakusui.actionunit.visitors;

import com.github.dakusui.actionunit.actions.*;
import com.github.dakusui.actionunit.core.Action;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class ActionScanner extends ActionWalker {
  @Override
  protected Consumer<Leaf> leafActionConsumer() {
    return leaf -> {
    };
  }

  @Override
  protected Consumer<Concurrent> concurrentActionConsumer() {
    return (Concurrent concurrent) -> {
      for (Action each : concurrent) {
        each.accept(this);
      }
    };
  }

  @Override
  protected <T> Consumer<ForEach<T>> forEachActionConsumer() {
    return (ForEach<T> a) -> a.createHandler(() -> {
      throw new UnsupportedOperationException();
    }).accept(this);
  }

  @Override
  protected <T> Consumer<While<T>> whileActionConsumer() {
    return (While<T> while$) -> {
      Supplier<T> value = () -> {
        throw new UnsupportedOperationException();
      };
      while$.createHandler(value).accept(ActionScanner.this);
    };
  }

  @Override
  protected <T> Consumer<When<T>> whenActionConsumer() {
    return (When<T> when) -> {
      Supplier<T> value = () -> {
        throw new UnsupportedOperationException();
      };
      //noinspection unchecked
      when.perform(value).accept(ActionScanner.this);
      when.otherwise(value).accept(ActionScanner.this);
    };
  }

  @Override
  protected Consumer<Retry> retryActionConsumer() {
    return (Retry retry) -> retry.action.accept(this);
  }

  @Override
  protected <T extends Throwable> Consumer<Attempt<T>> attemptActionConsumer() {
    return (Attempt<T> attempt) -> {
      attempt.attempt().accept(this);
      attempt.recover(() -> {
        throw new UnsupportedOperationException();
      }).accept(this);
      attempt.ensure().accept(this);
    };
  }

  @Override
  protected Consumer<TimeOut> timeOutActionConsumer() {
    return (TimeOut timeOut) -> timeOut.action.accept(this);
  }
}
