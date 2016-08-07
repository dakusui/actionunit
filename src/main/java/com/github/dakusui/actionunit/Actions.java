package com.github.dakusui.actionunit;

import java.util.concurrent.TimeUnit;

import static com.github.dakusui.actionunit.Utils.nonameIfNull;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static java.util.Arrays.asList;

/**
 * This class contains static utility methods that return objects of type {@code Action}.
 * How objects returned by methods in this class are performed is subject to actual implementations
 * of {@link com.github.dakusui.actionunit.Action.Visitor} interfaces, such as
 * {@link com.github.dakusui.actionunit.visitors.ActionRunner}.
 *
 * @see Action
 * @see com.github.dakusui.actionunit.Action.Visitor
 */
public enum Actions {
  ;

  /**
   * Creates a simple action object.
   *
   * @param runnable An object whose {@code run()} method run by a returned {@code Action} object.
   * @see com.github.dakusui.actionunit.Action.Leaf
   */
  public static Action simple(final Runnable runnable) {
    return simple(null, runnable);
  }

  /**
   * Creates a simple action object.
   *
   * @param summary  A string used by {@code describe()} method of a returned {@code Action} object.
   * @param runnable An object whose {@code run()} method run by a returned {@code Action} object.
   * @see com.github.dakusui.actionunit.Action.Leaf
   */
  public static Action simple(final String summary, final Runnable runnable) {
    checkNotNull(runnable);
    return new Action.Leaf() {
      @Override
      public String describe() {
        return nonameIfNull(summary);
      }

      @Override
      public void perform() {
        runnable.run();
      }
    };
  }

  /**
   * Creates an action which runs given {@code actions} in a concurrent manner.
   *
   * @param actions {@code Action} objects performed by returned {@code Action} object.
   * @see com.github.dakusui.actionunit.Action.Sequential
   */
  public static Action concurrent(Action... actions) {
    return concurrent(null, actions);
  }

  /**
   * Creates an action which runs given {@code actions} in a concurrent manner.
   *
   * @param summary A string used by {@code describe()} method of a returned {@code Action} object.
   * @param actions {@code Action} objects performed by a returned {@code Action} object.
   * @see com.github.dakusui.actionunit.Action.Sequential
   */
  public static Action concurrent(String summary, Action... actions) {
    return concurrent(summary, asList(actions));
  }

  /**
   * Creates an action which runs given {@code actions} in a concurrent manner.
   *
   * @param summary A string used by {@code describe()} method of a returned {@code Action} object.
   * @param actions {@code Action} objects performed by returned {@code Action} object.
   * @see com.github.dakusui.actionunit.Action.Sequential
   */
  public static Action concurrent(String summary, Iterable<? extends Action> actions) {
    return Action.Concurrent.Factory.INSTANCE.create(summary, actions);
  }

  /**
   * Creates an action which runs given {@code actions} in a sequential manner.
   *
   * @param actions {@code Action} objects performed by returned {@code Action} object.
   * @see com.github.dakusui.actionunit.Action.Sequential
   */
  public static Action sequential(Action... actions) {
    return sequential(null, actions);
  }

  /**
   * Creates an action which runs given {@code actions} in a sequential manner.
   *
   * @param summary A string used by {@code describe()} method of a returned {@code Action} object.
   * @param actions {@code Action} objects performed by returned {@code Action} object.
   * @see com.github.dakusui.actionunit.Action.Sequential
   */
  public static Action sequential(String summary, Action... actions) {
    return sequential(summary, asList(actions));
  }

  /**
   * Creates an action which runs given {@code actions} in a sequential manner.
   *
   * @param summary A string used by {@code describe()} method of a returned {@code Action} object.
   * @param actions {@code Action} objects performed by returned {@code Action} object.
   * @see com.github.dakusui.actionunit.Action.Sequential
   */
  public static Action sequential(String summary, Iterable<? extends Action> actions) {
    return Action.Sequential.Factory.INSTANCE.create(summary, actions);
  }

  /**
   * Creates an action object which times out after duration specified by given parameters.
   *
   * @param action   An action performed by the returned object.
   * @param duration A parameter to specify duration to time out with {@code timeUnit} parameter.
   * @param timeUnit Time unit of {@code duration}.
   */
  public static Action timeout(Action action, int duration, TimeUnit timeUnit) {
    checkNotNull(timeUnit);
    return new Action.TimeOut(action, TimeUnit.NANOSECONDS.convert(duration, timeUnit));
  }

  /**
   * Creates an action which retries given {@code action}.
   *
   * @param action   An action retried by the returned {@code Action}.
   * @param times    How many times given {@code action} will be retried. If 0 is given, no retry will happen.
   * @param interval Interval between actions.
   * @param timeUnit Time unit of {@code interval}.
   */
  public static Action retry(Action action, int times, int interval, TimeUnit timeUnit) {
    checkNotNull(timeUnit);
    return new Action.Retry(action, TimeUnit.NANOSECONDS.convert(interval, timeUnit), times);
  }

  /**
   * Performs an action created by given {@code factoryForActionWithTarget} for each element in
   * given {@code datasource}.
   *
   * @param datasource                 each of whose elements are processed by an action that
   *                                   {@code factoryForActionWithTarget} creates.
   * @param factoryForActionWithTarget creates an action that processes each element in {@code dataSource}.
   * @param <T>                        Type of entries in {@code datasource}.
   */
  public static <T> Action repeatIncrementally(
      Iterable<T> datasource, Action.WithTarget.Factory<T> factoryForActionWithTarget) {
    return new Action.RepeatIncrementally<>(datasource, factoryForActionWithTarget);
  }

  /**
   * Returns a factory which creates an action to process an object of type {@code T}.
   *
   * @param block specifies how to process a given object of type {@code T}.
   * @param <T>   Type of object processed that an action object processes created by returned factory.
   * @see Actions#repeatIncrementally(Iterable, Action.WithTarget.Factory)
   */
  public static <T> Action.WithTarget.Factory<T> forEach(final Block<T> block) {
    return forEach(null, block);
  }

  /**
   * Returns a factory which creates an action to process an object of type {@code T}.
   *
   * @param summary A string that describes returned factory. This will also be used to describe an
   *                action created by it.
   * @param block   specifies how to process a given object of type {@code T}.
   * @param <T>     Type of object processed that an action object processes created by returned factory.
   * @see Actions#repeatIncrementally(Iterable, Action.WithTarget.Factory)
   */
  public static <T> Action.WithTarget.Factory<T> forEach(final String summary, final Block<T> block) {
    checkNotNull(block);
    return new Action.WithTarget.Factory<T>() {
      @Override
      public Action create(T target) {
        return new Action.WithTarget<T>(target) {
          @Override
          public String describe() {
            return format("%s with %s", nonameIfNull(summary), target);
          }

          @Override
          protected void perform(T target) {
            block.apply(target);
          }
        };
      }

      @Override
      public String describe() {
        return nonameIfNull(summary);
      }
    };
  }

  /**
   * Returns an action that does nothing.
   */
  public static Action nop() {
    return nop(null);
  }

  /**
   * Returns an action that does nothing.
   *
   * @param summary A string that describes returned action.
   */
  public static Action nop(final String summary) {
    return new Action.Leaf() {
      @Override
      public void perform() {
      }

      @Override
      public String describe() {
        return nonameIfNull(summary);
      }
    };
  }

}
