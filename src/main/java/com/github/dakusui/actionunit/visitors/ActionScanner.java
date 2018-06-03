package com.github.dakusui.actionunit.visitors;

import com.github.dakusui.actionunit.actions.*;
import com.github.dakusui.actionunit.core.Action;

import java.util.function.Consumer;

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
    return (ForEach<T> a) -> a.createHandler(a.defaultValue()).accept(this);
  }

  @Override
  protected <T> Consumer<While<T>> whileActionConsumer() {
    return (While<T> while$) -> {
      while$.createAction().accept(ActionScanner.this);
    };
  }

  @Override
  protected <T> Consumer<When<T>> whenActionConsumer() {
    return (When<T> when) -> {
      //noinspection unchecked
      when.perform().accept(ActionScanner.this);
      when.otherwise().accept(ActionScanner.this);
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
      attempt.recover(ValueHolder.empty()).accept(this);
      attempt.ensure().accept(this);
    };
  }

  @Override
  protected Consumer<TimeOut> timeOutActionConsumer() {
    return (TimeOut timeOut) -> timeOut.action.accept(this);
  }
}
