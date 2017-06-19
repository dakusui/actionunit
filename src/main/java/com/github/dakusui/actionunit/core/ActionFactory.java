package com.github.dakusui.actionunit.core;

import com.github.dakusui.actionunit.actions.*;

import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * An interface that defines methods to create various actions and builders of
 * actions.
 */
public interface ActionFactory {
  IdGeneratorManager ID_GENERATOR_MANAGER = new IdGeneratorManager();

  default int generateId() {
    return ID_GENERATOR_MANAGER.generateId(this);
  }

  /**
   * Creates a simple action object.
   *
   * @param description A string used by {@code describable()} method of a returned {@code Action} object.
   * @param runnable    An object whose {@code run()} method run by a returned {@code Action} object.
   * @return Created action
   * @see Leaf
   */
  default Action simple(final String description, final Runnable runnable) {
    return ActionSupport.Internal.simple(generateId(), description, runnable);
  }

  /**
   * Creates a named action object.
   *
   * @param name   name of the action
   * @param action action to be named.
   * @return Created action
   */
  default Action named(String name, Action action) {
    return ActionSupport.Internal.named(generateId(), name, action);
  }

  /**
   * Creates an action which runs given {@code actions} in a concurrent manner.
   *
   * @param actions {@code Action} objects performed by returned {@code Action} object.
   * @return Created action
   * @see Concurrent
   */
  default Action concurrent(Action... actions) {
    return ActionSupport.Internal.concurrent(generateId(), actions);
  }

  /**
   * Creates an action which runs given {@code actions} in a concurrent manner.
   *
   * @param actions {@code Action} objects performed by returned {@code Action} object.
   * @return Created action
   * @see Concurrent
   */
  default Action concurrent(Iterable<? extends Action> actions) {
    return ActionSupport.Internal.concurrent(generateId(), actions);
  }

  /**
   * Creates an action which runs given {@code actions} in a sequential manner.
   *
   * @param actions {@code Action} objects performed by returned {@code Action} object.
   * @return Created action
   * @see Sequential
   */
  default Action sequential(Action... actions) {
    return ActionSupport.Internal.sequential(generateId(), actions);
  }

  /**
   * Creates an action which runs given {@code actions} in a sequential manner.
   *
   * @param actions {@code Action} objects performed by returned {@code Action} object.
   * @return Created action
   * @see Sequential
   */
  default Action sequential(Iterable<? extends Action> actions) {
    return ActionSupport.Internal.sequential(generateId(), actions);
  }

  /**
   * Returns an action that does nothing.
   *
   * @return Created action
   */
  default Action nop() {
    return ActionSupport.Internal.nop(generateId());
  }

  /**
   * Returns an action that does nothing.
   *
   * @param description A string that describes returned action.
   * @return Created action
   */
  default Action nop(final String description) {
    return ActionSupport.Internal.nop(generateId(), description);
  }

  /**
   * Returns an action that waits for given amount of time.
   *
   * @param duration Duration to wait for.
   * @param timeUnit Time unit of the {@code duration}.
   * @return Created action
   */
  default Action sleep(final long duration, final TimeUnit timeUnit) {
    return ActionSupport.Internal.sleep(generateId(), duration, timeUnit);
  }

  /**
   * Creates a builder for {@code ForEach} action.
   *
   * @param elements Elements iterated by a {@code ForEach} object that returned
   *                 builder will build.
   * @param <E>      Type of elements
   * @return Created builder
   * @see ForEach
   * @see ForEach.Builder
   */
  default <E> ForEach.Builder<E> forEachOf(Iterable<? extends E> elements) {
    return ActionSupport.Internal.forEachOf(generateId(), elements);
  }


  /**
   * Creates a builder for {@code ForEach} action.
   *
   * @param elements Elements iterated by a {@code ForEach} object that returned
   *                 builder will build.
   * @param <E>      Type of elements
   * @return Created builder
   * @see ForEach
   * @see ForEach.Builder
   */
  @SuppressWarnings("unchecked")
  default <E> ForEach.Builder<E> forEachOf(E... elements) {
    return ActionSupport.Internal.forEachOf(generateId(), elements);
  }

  /**
   * Creates a builder for {@code While} action. This method was named {@code whilst}
   * just because {@code while} is a reserved word in Java and it can't be used
   * for a method name.
   *
   * @param value     A supplier that gives value to be examined by {@code condition}.
   * @param condition A predicate that determines if an action created by {@code While}
   *                  object should be executed or not.
   * @param <T>       Type of value supplied by {@code value} and examined by {@code condition}
   * @return Created builder
   * @see While
   * @see While.Builder
   */
  default <T> While.Builder<T> whilst(Supplier<T> value, Predicate<T> condition) {
    return ActionSupport.Internal.whilst(generateId(), value, condition);
  }

  /**
   * Creates a builder for {@code When} action.
   *
   * @param value     A supplier that gives value to be examined by {@code condition}.
   * @param condition A predicate that determines if an action created by {@code While}
   *                  object should be executed or not.
   * @param <T>       Type of value supplied by {@code value} and examined by {@code condition}
   * @return Created builder
   * @see When
   * @see When.Builder
   */
  default <T> When.Builder<T> when(Supplier<T> value, Predicate<T> condition) {
    return ActionSupport.Internal.when(generateId(), value, condition);
  }

  /**
   * Creates a builder for {@code TimeOut} action.
   *
   * @param action An action executed by {@code TimeOut} action that returned builder
   *               builds
   * @return Created builder
   * @see TimeOut
   * @see TimeOut.Builder
   */
  default TimeOut.Builder timeout(Action action) {
    return ActionSupport.Internal.timeout(generateId(), action);
  }

  /**
   * Creates a builder for {@code Attempt} action.
   *
   * @param action An action executed by {@code Attempt} action that returned builder
   *               builds
   * @param <T>    Type of a {@code Throwable} that action may throw.
   * @return Created builder
   * @see Attempt
   * @see Attempt.Builder
   */
  default <T extends Throwable> Attempt.Builder<T> attempt(Action action) {
    return ActionSupport.Internal.attempt(generateId(), action);
  }

  /**
   * Creates a builder for {@code Retry} action.
   *
   * @param action An action executed by {@code Retry} action that returned builder
   *               builds
   * @return Created builder
   * @see Retry
   * @see Retry.Builder
   */
  default Retry.Builder retry(Action action) {
    return ActionSupport.Internal.retry(generateId(), action);
  }

  /**
   * Creates a builder for {@code TestAction} action. A function to be tested
   * will be passed to a {@code TestAction#when} method.
   *
   * @param description A string that describes a {@code given} supplier.
   * @param given       A supplier that gives a value to be tested by {@code Test} action.
   * @param <I>         Type of input to a function to be tested.
   * @param <O>         Type of output from a function to be tested.
   * @return Created builder
   * @see TestAction
   * @see TestAction.Builder
   */
  default <I, O> TestAction.Builder<I, O> given(String description, Supplier<I> given) {
    return ActionSupport.Internal.given(generateId(), description, given);
  }
}
