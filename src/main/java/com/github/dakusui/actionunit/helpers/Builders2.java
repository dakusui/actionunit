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

  @SuppressWarnings("unchecked")
  default <E> ForEach.Builder<E> forEachOf(E... elements) {
    return Builders.forEachOf(elements);
  }

  default <T> While2.Builder<T> loopWhile(Supplier<T> value, Predicate<T> condition) {
    return Builders.repeatWhile(value, condition);
  }

  default <T> When2.Builder<T> when(Supplier<T> value, Predicate<T> condition) {
    return Builders.when(value, condition);
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

  default <I, O> TestAction.Builder<I, O> given(String description, Supplier<I> given) {
    return Builders.given(description, given);
  }

  default <T> HandlerFactory<T> handlerFactory(String description, Consumer<T> handlerBody) {
    return Builders.handlerFactory(description, handlerBody);
  }
}
