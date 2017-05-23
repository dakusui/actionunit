package com.github.dakusui.actionunit;

import com.github.dakusui.actionunit.actions.*;
import com.github.dakusui.actionunit.exceptions.ActionException;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static com.github.dakusui.actionunit.Checks.checkArgument;
import static com.github.dakusui.actionunit.Checks.checkNotNull;
import static com.github.dakusui.actionunit.Utils.nonameIfNull;
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
   * @param runnable An object whose {@code run()} method run by a returned {@code Action} object.
   * @see Leaf
   */
  public static Action simple(final Runnable runnable) {
    checkNotNull(runnable);
    return new Leaf() {
      @Override
      public void perform() {
        runnable.run();
      }

      @Override
      public String toString() {
        return Utils.describe(runnable);
      }
    };
  }

  /**
   * Creates a simple action object.
   *
   * @param summary  A string used by {@code describe()} method of a returned {@code Action} object.
   * @param runnable An object whose {@code run()} method run by a returned {@code Action} object.
   * @see Leaf
   */
  public static Action simple(final String summary, final Runnable runnable) {
    return named(summary, simple(runnable));
  }

  /**
   * Creates a named action object.
   *
   * @param name   name of the action
   * @param action action to be named.
   */
  public static Action named(String name, Action action) {
    return Named.Factory.create(name, action);
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
   * @param summary A string used by {@code describe()} method of a returned {@code Action} object.
   * @param actions {@code Action} objects performed by a returned {@code Action} object.
   * @see Sequential.Impl
   */
  public static Action concurrent(String summary, Action... actions) {
    return concurrent(summary, asList(actions));
  }

  /**
   * Creates an action which runs given {@code actions} in a concurrent manner.
   *
   * @param summary A string used by {@code describe()} method of a returned {@code Action} object.
   * @param actions {@code Action} objects performed by returned {@code Action} object.
   * @see Sequential.Impl
   */
  public static Action concurrent(String summary, Iterable<? extends Action> actions) {
    return named(summary, concurrent(actions));
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
   * Creates an action which runs given {@code actions} in a sequential manner.
   *
   * @param summary A string used by {@code describe()} method of a returned {@code Action} object.
   * @param actions {@code Action} objects performed by returned {@code Action} object.
   * @see Sequential.Impl
   */
  public static Action sequential(String summary, Action... actions) {
    return sequential(summary, asList(actions));
  }

  /**
   * Creates an action which runs given {@code actions} in a sequential manner.
   *
   * @param summary A string used by {@code describe()} method of a returned {@code Action} object.
   * @param actions {@code Action} objects performed by returned {@code Action} object.
   * @see Sequential.Impl
   */
  public static Action sequential(String summary, Iterable<? extends Action> actions) {
    return named(summary, sequential(actions));
  }

  /**
   * Creates an action object which times out after duration specified by given parameters.
   *
   * @param action   An action performed by the returned object.
   * @param duration A parameter to specify duration to time out with {@code timeUnit} parameter.
   * @param timeUnit Time unit of {@code duration}.
   */
  public static Action timeout(Action action, long duration, TimeUnit timeUnit) {
    checkNotNull(timeUnit);
    return new TimeOut(action, NANOSECONDS.convert(duration, timeUnit));
  }

  /**
   * Creates an action which retries given {@code action}.
   *
   * @param targetExceptionClass Exception class to be traped by returned {@code Action}.
   * @param action               An action retried by the returned {@code Action}.
   * @param times                How many times given {@code action} will be retried. If 0 is given, no retry will happen.
   *                             If {@link Retry#INFINITE} is given, returned
   *                             action will re-try infinitely until {@code action} successes.
   * @param interval             Interval between actions.
   * @param timeUnit             Time unit of {@code interval}.
   */
  public static <T extends Throwable> Action retry(Class<T> targetExceptionClass, Action action, int times, long interval, TimeUnit timeUnit) {
    checkNotNull(timeUnit);
    //noinspection unchecked
    return new Retry(targetExceptionClass, action, NANOSECONDS.convert(interval, timeUnit), times);
  }

  /**
   * Creates an action which retries given {@code action}.
   *
   * @param action   An action retried by the returned {@code Action}.
   * @param times    How many times given {@code action} will be retried. If 0 is given, no retry will happen.
   *                 If {@link Retry#INFINITE} is given, returned
   *                 action will re-try infinitely until {@code action} successes.
   * @param interval Interval between actions.
   * @param timeUnit Time unit of {@code interval}.
   */
  public static Action retry(Action action, int times, long interval, TimeUnit timeUnit) {
    return retry(ActionException.class, action, times, interval, timeUnit);
  }

  public static <T> Action foreach(HandlerFactory<T> handlerFactory, ForEach.Mode mode, Iterable<T> dataSource) {
    return new ForEach.Impl<T>(handlerFactory, dataSource, Objects.requireNonNull(mode).getFactory());
  }

  public static Action repeatwhile(Predicate<?> condition, Action... actions) {
    Action action = nop();
    if (actions.length == 1) {
      action = actions[0];
    } else if (actions.length > 1) {
      action = sequential(actions);
    }
    return new While.Impl(condition, action);
  }

  public static Action when(Predicate<?> condition, Action action) {
    return new When.Impl(condition, action, nop());
  }

  public static Action when(Predicate<?> condition, Action action, Action otherwise) {
    return new When.Impl(condition, action, otherwise);
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

  public static <E extends Throwable> Attempt.Builder<E> attempt(Action attempt) {
    return new Attempt.Builder<>(attempt);
  }

    public static <I, O> TestAction.Builder<I, O> test() {
    return new TestAction.Builder<>();
  }

}
