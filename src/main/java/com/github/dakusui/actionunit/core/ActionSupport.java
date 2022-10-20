package com.github.dakusui.actionunit.core;

import com.github.dakusui.actionunit.actions.*;
import com.github.dakusui.actionunit.actions.cmd.CommanderInitializer;
import com.github.dakusui.actionunit.actions.cmd.unix.Cmd;
import com.github.dakusui.actionunit.core.context.ContextConsumer;
import com.github.dakusui.actionunit.core.context.StreamGenerator;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.dakusui.actionunit.core.context.ContextConsumer.NOP_CONSUMER;
import static java.util.Arrays.asList;

public enum ActionSupport {
  ;

  public static Action nop() {
    // Needs to be instantiated each time this method is called.
    // Otherwise, multiple nops cannot be identified in an action tree.
    return Leaf.of(NOP_CONSUMER);
  }

  public static Action leaf(Consumer<Context> consumer) {
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

  public static <T> With.Builder<T> with(Function<Context, T> value) {
    return new With.Builder<>("i", value);
  }

  /**
   * Note that `variableName` won't be used to resolve a value of a variable, it is
   * merely intended to be printed in an action-tree or logs.
   *
   * @param variableName human-readable variable name.
   * @param value        A function to give a value to be used a context under the returned action.
   * @param <T>          The type of the variable
   * @return A builder for a `with` action.
   */
  public static <T> With.Builder<T> with(String variableName, Function<Context, T> value) {
    return new With.Builder<>(variableName, value);
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

