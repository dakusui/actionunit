package com.github.dakusui.actionunit;

import com.github.dakusui.actionunit.actions.Attempt;
import com.github.dakusui.actionunit.actions.ForEach;

import static java.util.Arrays.asList;

public enum Builders {
  ;
  public static <E> ForEach.Builder<E> foreach(Iterable<? extends E> elements) {
    return ForEach.builder(elements);
  }

  @SafeVarargs
  public static <E> ForEach.Builder<E> foreach(E... elements) {
    return ForEach.builder(asList(elements));
  }

  public static <T extends Throwable> Attempt.Builder<T> attempt(Action attemptee) {
    return Attempt.builder(attemptee);
  }
}
