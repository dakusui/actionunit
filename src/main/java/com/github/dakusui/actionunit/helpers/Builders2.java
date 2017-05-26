package com.github.dakusui.actionunit.helpers;

import com.github.dakusui.actionunit.actions.*;
import com.github.dakusui.actionunit.core.Action;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface Builders2 {

  default <E> ForEach.Builder<E> forEachOf(Iterable<? extends E> elements) {
    return Builders.forEachOf(elements);
  }

  default <E> ForEach.Builder<E> forEachOf(E... elements) {
    return Builders.forEachOf(elements);
  }

  default TimeOut.Builder timeout(Action action) {
    return Builders.timeout(action);
  }

  default <T extends Throwable> Attempt.Builder<T> attempt(Action action) {
    return Builders.attempt(action);
  }

  default Retry.Builder retry(Action action) {
    return Builders.retry(action);
  }

  default <T> While.Builder<T> loopWhile(Predicate<T> condition) {
    return Builders.loopWhile(condition);
  }

  default <I, O> TestAction.Builder<I, O> given(String description, Supplier<I> given) {
    return Builders.given(description, given);
  }

  default <T> When.Builder<T> when(Predicate<T> condition) {
    return Builders.when(condition);
  }

  default <T> HandlerFactory<T> handlerFactory(String description, Consumer<T> handlerBody) {
    return Builders.handlerFactory(description, handlerBody);
  }
}
