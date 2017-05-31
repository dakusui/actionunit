package com.github.dakusui.actionunit.core;

import com.github.dakusui.actionunit.actions.*;
import com.github.dakusui.actionunit.helpers.ActionSupport;

import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface ActionFactory {
  /**
   * Creates a simple action object.
   *
   * @param description A string used by {@code describe()} method of a returned {@code Action} object.
   * @param runnable    An object whose {@code run()} method run by a returned {@code Action} object.
   * @see Leaf
   */
  default Action simple(final String description, final Runnable runnable) {
    return ActionSupport.simple(description, runnable);
  }

  /**
   * Creates a named action object.
   *
   * @param name   name of the action
   * @param action action to be named.
   */
  default Action named(String name, Action action) {
    return ActionSupport.named(name, action);
  }

  /**
   * Creates an action which runs given {@code actions} in a concurrent manner.
   *
   * @param actions {@code Action} objects performed by returned {@code Action} object.
   * @see Sequential.Impl
   */
  default Action concurrent(Action... actions) {
    return ActionSupport.concurrent(actions);
  }

  /**
   * Creates an action which runs given {@code actions} in a concurrent manner.
   *
   * @param actions {@code Action} objects performed by returned {@code Action} object.
   * @see Sequential.Impl
   */
  default Action concurrent(Iterable<? extends Action> actions) {
    return ActionSupport.concurrent(actions);
  }

  /**
   * Creates an action which runs given {@code actions} in a sequential manner.
   *
   * @param actions {@code Action} objects performed by returned {@code Action} object.
   * @see Sequential.Impl
   */
  default Action sequential(Action... actions) {
    return ActionSupport.sequential(actions);
  }

  /**
   * Creates an action which runs given {@code actions} in a sequential manner.
   *
   * @param actions {@code Action} objects performed by returned {@code Action} object.
   * @see Sequential.Impl
   */
  default Action sequential(Iterable<? extends Action> actions) {
    return ActionSupport.sequential(actions);
  }

  /**
   * Returns an action that does nothing.
   */
  default Action nop() {
    return ActionSupport.nop();
  }

  /**
   * Returns an action that does nothing.
   *
   * @param description A string that describes returned action.
   */
  default Action nop(final String description) {
    return ActionSupport.nop(description);
  }

  /**
   * Returns an action that waits for given amount of time.
   *
   * @param duration Duration to wait for.
   * @param timeUnit Time unit of the {@code duration}.
   */
  default Action sleep(final long duration, final TimeUnit timeUnit) {
    return ActionSupport.sleep(duration, timeUnit);
  }

  default <E> ForEach.Builder<E> forEachOf(Iterable<? extends E> elements) {
    return ActionSupport.forEachOf(elements);
  }

  @SuppressWarnings("unchecked")
  default <E> ForEach.Builder<E> forEachOf(E... elements) {
    return ActionSupport.forEachOf(elements);
  }

  default <T> While.Builder<T> whilst(Supplier<T> value, Predicate<T> condition) {
    return ActionSupport.whilst(value, condition);
  }

  default <T> When.Builder<T> when(Supplier<T> value, Predicate<T> condition) {
    return ActionSupport.when(value, condition);
  }

  default TimeOut.Builder timeout(Action action) {
    return ActionSupport.timeout(action);
  }

  default <T extends Throwable> Attempt.Builder<T> attempt(Action action) {
    return ActionSupport.attempt(action);
  }

  default Retry.Builder retry(Action action) {
    return ActionSupport.retry(action);
  }

  default <I, O> TestAction.Builder<I, O> given(String description, Supplier<I> given) {
    return ActionSupport.given(description, given);
  }
}
