package com.github.dakusui.actionunit.core;

import com.github.dakusui.actionunit.actions.*;
import com.github.dakusui.actionunit.helpers.InternalUtils;

import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.github.dakusui.actionunit.helpers.Checks.checkArgument;
import static com.github.dakusui.actionunit.helpers.Checks.checkNotNull;
import static com.github.dakusui.actionunit.helpers.InternalUtils.nonameIfNull;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * This class contains static utility methods that return objects of type {@code Action}.
 * How objects returned by methods in this class are performed is subject to actual implementations
 * of {@link Action.Visitor} interfaces, such as
 * {@link com.github.dakusui.actionunit.visitors.ActionPerformer}.
 *
 * @see Action
 * @see Action.Visitor
 */
public enum ActionSupport {
  ;

  /**
   * Creates a simple action object.
   *
   * @param description A string used by {@code describable()} method of a returned {@code Action} object.
   * @param runnable    An object whose {@code run()} method run by a returned {@code Action} object.
   * @return Created action
   * @see Leaf
   */
  public static Action simple(final String description, final Runnable runnable) {
    return Leaf.create(description, runnable);
  }

  /**
   * Creates a named action object.
   *
   * @param name   name of the action
   * @param action action to be named.
   * @return Created action
   */
  public static Action named(String name, Action action) {
    return Named.create(name, action);
  }

  /**
   * Creates an action which runs given {@code actions} in a concurrent manner.
   *
   * @param actions {@code Action} objects performed by returned {@code Action} object.
   * @return Created action
   * @see Concurrent
   */
  public static Action concurrent(Action... actions) {
    return concurrent(asList(actions));
  }

  /**
   * Creates an action which runs given {@code actions} in a concurrent manner.
   *
   * @param actions {@code Action} objects performed by returned {@code Action} object.
   * @return Created action
   * @see Concurrent
   */
  public static Action concurrent(Iterable<? extends Action> actions) {
    return Concurrent.Factory.INSTANCE.create(actions);
  }

  /**
   * Creates an action which runs given {@code actions} in a sequential manner.
   *
   * @param actions {@code Action} objects performed by returned {@code Action} object.
   * @return Created action
   * @see Sequential
   */
  public static Action sequential(Action... actions) {
    return sequential(asList(actions));
  }

  /**
   * Creates an action which runs given {@code actions} in a sequential manner.
   *
   * @param actions {@code Action} objects performed by returned {@code Action} object.
   * @return Created action
   * @see Sequential
   */
  public static Action sequential(Iterable<? extends Action> actions) {
    return Sequential.Factory.INSTANCE.create(actions);
  }

  /**
   * Returns an action that does nothing.
   *
   * @return Created action
   */
  public static Action nop() {
    return nop("(nop)");
  }

  /**
   * Returns an action that does nothing.
   *
   * @param description A string that describes returned action.
   * @return Created action
   */
  public static Action nop(final String description) {
    return new Leaf() {
      @Override
      public void perform() {
      }

      @Override
      public String toString() {
        return nonameIfNull(description);
      }
    };
  }

  /**
   * Returns an action that waits for given amount of time.
   *
   * @param duration Duration to wait for.
   * @param timeUnit Time unit of the {@code duration}.
   * @return Created action
   */
  public static Action sleep(final long duration, final TimeUnit timeUnit) {
    checkArgument(duration >= 0, "duration must be non-negative but %s was given", duration);
    checkNotNull(timeUnit);
    return new Leaf() {
      @Override
      public void perform() {
        InternalUtils.sleep(duration, timeUnit);
      }

      @Override
      public String toString() {
        return format("sleep for %s", InternalUtils.formatDuration(NANOSECONDS.convert(duration, timeUnit)));
      }
    };
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
  public static <E> ForEach.Builder<E> forEachOf(Iterable<? extends E> elements) {
    return ForEach.builder(elements);
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
  @SafeVarargs
  public static <E> ForEach.Builder<E> forEachOf(E... elements) {
    return ForEach.builder(asList(elements));
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
  public static <T> While.Builder<T> whilst(Supplier<T> value, Predicate<T> condition) {
    return new While.Builder<>(value, condition);
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
  public static <T> When.Builder<T> when(Supplier<T> value, Predicate<T> condition) {
    return new When.Builder<>(value, condition);
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
  public static TimeOut.Builder timeout(Action action) {
    return new TimeOut.Builder(action);
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
  public static <T extends Throwable> Attempt.Builder<T> attempt(Action action) {
    return Attempt.builder(action);
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
  public static Retry.Builder retry(Action action) {
    return Retry.builder(action);
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
  public static <I, O> TestAction.Builder<I, O> given(String description, Supplier<I> given) {
    return new TestAction.Builder<I, O>().given(description, given);
  }
}
