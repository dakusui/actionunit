package com.github.dakusui.actionunit.core;

import com.github.dakusui.actionunit.actions.*;
import com.github.dakusui.actionunit.actions.cmd.Commander;

import java.util.Formatter;
import java.util.List;

import static java.util.Arrays.asList;

public enum ActionSupport {
  ;

  public static Action nop() {
    // Needs to be instantiated each time this method is called.
    // Otherwise, multiple nops cannot be identified in an action tree.
    return Leaf.of(new ContextConsumer() {
      @Override
      public void accept(Context context) {
      }

      @Override
      public void formatTo(Formatter formatter, int flags, int width, int precision) {
        formatter.format("(nop)");
      }
    });
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

  public static <E> ForEach.Builder<E> forEach(String variableName, DataSupplier<E> dataSupplier) {
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

