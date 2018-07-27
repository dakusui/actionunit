package com.github.dakusui.actionunit.n.utils;

import com.github.dakusui.actionunit.n.actions.*;
import com.github.dakusui.actionunit.n.core.Action;
import com.github.dakusui.actionunit.n.core.context.ContextConsumer;
import com.github.dakusui.actionunit.n.core.context.ContextPredicate;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

public enum ActionSupport {
  ;

  public static Action nop() {
    return Leaf.NOP;
  }

  public static Action leaf(ContextConsumer consumer) {
    return Leaf.of(consumer);
  }

  public static Action named(String name, Action action) {
    return Named.of(name, action);
  }

  public static Attempt.Builder attempt(Action perform) {
    return new Attempt.Builder(perform);
  }

  public static <E> ForEach.Builder<E> forEach(String variableName, Supplier<Stream<E>> dataSupplier) {
    return new ForEach.Builder<>(variableName, dataSupplier);
  }

  public static When.Builder when(ContextPredicate cond) {
    return new When.Builder(cond);
  }

  public static Retry.Builder retry(Action target) {
    return new Retry.Builder(target);
  }

  public static TimeOut.Builder timeout(Action target) {
    return new TimeOut.Builder(target);
  }

  public static Composite sequential(Action... actions) {
    return sequential(asList(actions));
  }

  public static Composite sequential(List<Action> actions) {
    return new Composite.Builder(actions).build();
  }

  public static Composite parallel(Action... actions) {
    return parallel(asList(actions));
  }

  public static Composite parallel(List<Action> actions) {
    return new Composite.Builder(actions).parallel().build();
  }
}
