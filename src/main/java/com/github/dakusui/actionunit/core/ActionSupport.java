package com.github.dakusui.actionunit.core;

import com.github.dakusui.actionunit.actions.*;
import com.github.dakusui.actionunit.actions.cmd.CommanderInitializer;
import com.github.dakusui.actionunit.actions.cmd.unix.Cmd;
import com.github.dakusui.actionunit.core.context.ContextConsumer;
import com.github.dakusui.actionunit.core.context.ContextPredicate;
import com.github.dakusui.actionunit.core.context.StreamGenerator;

import java.util.Formatter;
import java.util.List;
import java.util.function.Predicate;

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

  public static <E> ForEach.Builder<E> forEach(String variableName, StreamGenerator<E> streamGenerator) {
    return new ForEach.Builder<>(variableName, streamGenerator);
  }

  public static While.Builder repeatWhile(Predicate<Context> condition) {
    return new While.Builder(condition);
  }

  public static When.Builder when(Predicate<Context> cond) {
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

  public static Cmd cmd(String program, String... knownVariables) {
    return cmd(program, CommanderInitializer.DEFAULT_INSTANCE, knownVariables);
  }

  public static Cmd cmd(String program, CommanderInitializer initializer, String... knownVariables) {
    Cmd ret = new Cmd(initializer).command(program);
    for (String each : knownVariables)
      ret = ret.declareVariable(each);
    return ret;
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

