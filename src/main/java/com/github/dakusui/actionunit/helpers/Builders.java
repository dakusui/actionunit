package com.github.dakusui.actionunit.helpers;

import com.github.dakusui.actionunit.actions.*;
import com.github.dakusui.actionunit.core.Action;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Arrays.asList;

public enum Builders {
  ;

  public static <E> ForEach.Builder<E> forEachOf(Iterable<? extends E> elements) {
    return ForEach.builder(elements);
  }

  @SafeVarargs
  public static <E> ForEach.Builder<E> forEachOf(E... elements) {
    return ForEach.builder(asList(elements));
  }

  public static TimeOut.Builder timeout(Action action) {
    return new TimeOut.Builder(action);
  }

  public static <T extends Throwable> Attempt.Builder<T> attempt(Action action) {
    return Attempt.builder(action);
  }

  public static Retry.Builder retry(Action action) {
    return Retry.builder(action);
  }

  public static <T> While.Builder<T> loopWhile(Predicate<T> condition) {
    return new While.Builder<>(condition);
  }

  public static <I, O> TestAction.Builder<I, O> given(String description, Supplier<I> given) {
    return new TestAction.Builder<I, O>().given(description, given);
  }

  public static <T> When.Builder<T> when(Predicate<T> condition) {
    return new When.Builder<>(condition);
  }

  public static <T> HandlerFactory<T> handlerFactory(String description, Consumer<T> handlerBody) {
    return HandlerFactory.create(description, handlerBody);
  }
}
