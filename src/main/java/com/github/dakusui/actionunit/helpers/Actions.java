package com.github.dakusui.actionunit.helpers;

import com.github.dakusui.actionunit.actions.*;
import com.github.dakusui.actionunit.core.Action;

import java.util.concurrent.TimeUnit;

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
 * {@link com.github.dakusui.actionunit.visitors.ActionRunner}.
 *
 * @see Action
 * @see Action.Visitor
 */
public enum Actions {
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
}
