package com.github.dakusui.actionunit.n.core;

import com.github.dakusui.actionunit.n.actions.*;
import com.github.dakusui.actionunit.n.actions.cmd.Commander;

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

  public static Attempt.Builder attempt(Action action) {
    return new Attempt.Builder(action);
  }

  public static <E> ForEach.Builder<E> forEach(String variableName, Supplier<Stream<E>> dataSupplier) {
    return new ForEach.Builder<>(variableName, dataSupplier);
  }

  public static When.Builder when(ContextPredicate cond) {
    return new When.Builder(cond);
  }

  public static Retry.Builder retry(Action action) {
    return new Retry.Builder(action);
  }

  public static TimeOut.Builder timeout(Action action) {
    return new TimeOut.Builder(action);
  }

  public static Action sequential(List<Action> actions) {
    return new Composite.Builder(actions).build();
  }

  public static Action parallel(List<Action> actions) {
    return new Composite.Builder(actions).parallel().build();
  }

  public static Commander cmd(String program) {
    return new Commander() {
      @Override
      protected String program() {
        return program;
      }
    };
  }

  public static Action simple(String name, ContextConsumer consumer) {
    return named(name, leaf(consumer));
  }

  public static Action sequential(Action... actions) {
    return sequential(asList(actions));
  }

  public static Action parallel(Action... actions) {
    return parallel(asList(actions));
  }
}

