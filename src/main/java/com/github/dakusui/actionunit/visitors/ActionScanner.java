package com.github.dakusui.actionunit.visitors;

import com.github.dakusui.actionunit.actions.*;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.AutocloseableIterator;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class ActionScanner extends ActionWalker {
  @Override
  Consumer<Leaf> leafActionConsumer() {
    return this::handleAction;
  }

  @Override
  Consumer<Concurrent> concurrentActionConsumer() {
    return (Concurrent a) -> {
      try (AutocloseableIterator<Action> i = a.iterator()) {
        while (i.hasNext()) {
          i.next().accept(this);
        }
      }
    };
  }

  @Override
  <T> Consumer<ForEach<T>> forEachActionConsumer() {
    return (ForEach<T> a) -> a.createHandler(() -> {
      throw new UnsupportedOperationException();
    }).accept(this);
  }

  @Override
  <T> Consumer<While<T>> whileActionConsumer() {
    return (While<T> while$) -> {
      Supplier<T> value = () -> {
        throw new UnsupportedOperationException();
      };
      while$.createHandler(value).accept(ActionScanner.this);
    };
  }

  @Override
  <T> Consumer<When<T>> whenActionConsumer() {
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
  Consumer<Retry> retryActionConsumer() {
    return (Retry retry) -> retry.action.accept(this);
  }

  @Override
  <T extends Throwable> Consumer<Attempt<T>> attemptActionConsumer() {
    return (Attempt<T> attempt) -> {
      handleAction(attempt);
      attempt.attempt().accept(this);
      attempt.recover(() -> {
        throw new UnsupportedOperationException();
      }).accept(this);
      attempt.ensure().accept(this);
    };
  }

  @Override
  Consumer<TimeOut> timeOutActionConsumer() {
    return (TimeOut timeOut) -> timeOut.action.accept(this);
  }

  void handleAction(Action a) {
  }
}
