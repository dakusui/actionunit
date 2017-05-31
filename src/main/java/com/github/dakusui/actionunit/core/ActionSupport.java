package com.github.dakusui.actionunit.core;

import com.github.dakusui.actionunit.actions.*;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.helpers.Utils;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.github.dakusui.actionunit.helpers.Checks.checkArgument;
import static com.github.dakusui.actionunit.helpers.Checks.checkNotNull;
import static com.github.dakusui.actionunit.helpers.Utils.nonameIfNull;
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
   * @param description A string used by {@code describe()} method of a returned {@code Action} object.
   * @param runnable    An object whose {@code run()} method run by a returned {@code Action} object.
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
   */
  public static Action named(String name, Action action) {
    return Named.create(name, action);
  }

  /**
   * Creates an action which runs given {@code actions} in a concurrent manner.
   *
   * @param actions {@code Action} objects performed by returned {@code Action} object.
   * @see Sequential.Impl
   */
  public static Action concurrent(Action... actions) {
    return concurrent(asList(actions));
  }

  /**
   * Creates an action which runs given {@code actions} in a concurrent manner.
   *
   * @param actions {@code Action} objects performed by returned {@code Action} object.
   * @see Sequential.Impl
   */
  public static Action concurrent(Iterable<? extends Action> actions) {
    return Concurrent.Factory.INSTANCE.create(actions);
  }

  /**
   * Creates an action which runs given {@code actions} in a sequential manner.
   *
   * @param actions {@code Action} objects performed by returned {@code Action} object.
   * @see Sequential.Impl
   */
  public static Action sequential(Action... actions) {
    return sequential(asList(actions));
  }

  /**
   * Creates an action which runs given {@code actions} in a sequential manner.
   *
   * @param actions {@code Action} objects performed by returned {@code Action} object.
   * @see Sequential.Impl
   */
  public static Action sequential(Iterable<? extends Action> actions) {
    return Sequential.Factory.INSTANCE.create(actions);
  }

  /**
   * Returns an action that does nothing.
   */
  public static Action nop() {
    return nop("(nop)");
  }

  /**
   * Returns an action that does nothing.
   *
   * @param summary A string that describes returned action.
   */
  public static Action nop(final String summary) {
    return new Leaf() {
      @Override
      public void perform() {
      }

      @Override
      public String toString() {
        return nonameIfNull(summary);
      }
    };
  }

  /**
   * Returns an action that waits for given amount of time.
   *
   * @param duration Duration to wait for.
   * @param timeUnit Time unit of the {@code duration}.
   */
  public static Action sleep(final long duration, final TimeUnit timeUnit) {
    checkArgument(duration >= 0, "duration must be non-negative but %s was given", duration);
    checkNotNull(timeUnit);
    return new Leaf() {
      @Override
      public void perform() {
        Utils.sleep(duration, timeUnit);
      }

      @Override
      public String toString() {
        return format("sleep for %s", Utils.formatDuration(NANOSECONDS.convert(duration, timeUnit)));
      }
    };
  }

  public static <E> ForEach.Builder<E> forEachOf(Iterable<? extends E> elements) {
    return ForEach.builder(elements);
  }

  @SafeVarargs
  public static <E> ForEach.Builder<E> forEachOf(E... elements) {
    return ForEach.builder(asList(elements));
  }

  public static TimeOut.Builder timeout(Action action) {
    return new TimeOut.Builder(action);
  }

  public static <T extends Throwable> Attempt.Builder<T> attempt(Action action) {
    return Attempt.builder(action);
  }

  public static Retry.Builder retry(Action action) {
    return Retry.builder(action);
  }

  public static <T> While.Builder<T> whilst(Supplier<T> value, Predicate<T> condition) {
    return new While.Builder<>(value, condition);
  }

  public static <I, O> TestAction.Builder<I, O> given(String description, Supplier<I> given) {
    return new TestAction.Builder<I, O>().given(description, given);
  }

  public static <T> When.Builder<T> when(Supplier<T> value, Predicate<T> condition) {
    return new When.Builder<>(value, condition);
  }

  public static <T> HandlerFactory<T> handlerFactory(String description, Consumer<T> handlerBody) {
    return HandlerFactory.create(description, handlerBody);
  }
}
