package com.github.dakusui.actionunit;

import com.github.dakusui.actionunit.actions.Attempt;
import com.github.dakusui.actionunit.actions.ForEach;
import com.github.dakusui.actionunit.actions.HandlerFactory;

import java.util.function.Consumer;

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
}
