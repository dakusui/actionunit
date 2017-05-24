package com.github.dakusui.actionunit.helpers;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.actions.*;

import java.util.function.Consumer;
import java.util.function.Predicate;

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

  public static <T extends Throwable> Attempt.Builder<T> attempt(Action attemptee) {
    return Attempt.builder(attemptee);
  }

  public static <T> HandlerFactory<T> handlerFactory(String description, Consumer<T> handlerBody) {
    return HandlerFactory.create(description, handlerBody);
  }

  public static Retry.Builder retry(Action action) {
    return Retry.builder(action);
  }

  public static <I, O> TestAction.Builder<I, O> verify() {
    return new TestAction.Builder<>();
  }

  public static When.Builder when(Predicate<?> condition) {
    return new When.Builder<>(condition);
  }
}
