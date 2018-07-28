package com.github.dakusui.actionunit.n.utils;

import com.github.dakusui.actionunit.n.actions.*;
import com.github.dakusui.actionunit.n.actions.cmd.Commander;
import com.github.dakusui.actionunit.n.core.Action;
import com.github.dakusui.actionunit.n.core.context.ContextConsumer;
import com.github.dakusui.actionunit.n.core.context.ContextPredicate;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

public class ActionSupport extends Core {
  private ActionSupport() {
  }

  public static Action simple(String name, ContextConsumer consumer) {
    return Core.named(name, Core.leaf(consumer));
  }

  public static Action sequential(Action... actions) {
    return Core.sequential(asList(actions));
  }

  public static Action parallel(Action... actions) {
    return Core.parallel(asList(actions));
  }
}

class Core {
  Core() {
  }

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
}

