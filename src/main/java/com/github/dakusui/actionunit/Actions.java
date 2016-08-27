package com.github.dakusui.actionunit;

import com.github.dakusui.actionunit.connectors.Connectors;
import com.github.dakusui.actionunit.connectors.Pipe;
import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.actionunit.connectors.Source;
import com.google.common.base.Function;

import java.util.concurrent.TimeUnit;

import static com.github.dakusui.actionunit.Action.ForEach.Mode.SEQUENTIALLY;
import static com.github.dakusui.actionunit.Utils.nonameIfNull;
import static com.github.dakusui.actionunit.Utils.transform;
import static com.github.dakusui.actionunit.connectors.Connectors.toPipe;
import static com.github.dakusui.actionunit.connectors.Connectors.toSource;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

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
    return new Action.Leaf(nonameIfNull(summary)) {
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
   * @see Action.Sequential.Base
   */
  public static Action concurrent(Action... actions) {
    return concurrent(null, actions);
  }

  /**
   * Creates an action which runs given {@code actions} in a concurrent manner.
   *
   * @param summary A string used by {@code describe()} method of a returned {@code Action} object.
   * @param actions {@code Action} objects performed by a returned {@code Action} object.
   * @see Action.Sequential.Base
   */
  public static Action concurrent(String summary, Action... actions) {
    return concurrent(summary, asList(actions));
  }

  /**
   * Creates an action which runs given {@code actions} in a concurrent manner.
   *
   * @param summary A string used by {@code describe()} method of a returned {@code Action} object.
   * @param actions {@code Action} objects performed by returned {@code Action} object.
   * @see Action.Sequential.Base
   */
  public static Action concurrent(String summary, Iterable<? extends Action> actions) {
    return Action.Concurrent.Factory.INSTANCE.create(summary, actions);
  }

  /**
   * Creates an action which runs given {@code actions} in a sequential manner.
   *
   * @param actions {@code Action} objects performed by returned {@code Action} object.
   * @see Action.Sequential.Base
   */
  public static Action sequential(Action... actions) {
    return sequential(null, actions);
  }

  /**
   * Creates an action which runs given {@code actions} in a sequential manner.
   *
   * @param summary A string used by {@code describe()} method of a returned {@code Action} object.
   * @param actions {@code Action} objects performed by returned {@code Action} object.
   * @see Action.Sequential.Base
   */
  public static Action sequential(String summary, Action... actions) {
    return sequential(summary, asList(actions));
  }

  public static Action sequential(Iterable<Action> actions) {
    return sequential(null, actions);
  }

  /**
   * Creates an action which runs given {@code actions} in a sequential manner.
   *
   * @param summary A string used by {@code describe()} method of a returned {@code Action} object.
   * @param actions {@code Action} objects performed by returned {@code Action} object.
   * @see Action.Sequential.Base
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
  public static Action timeout(Action action, long duration, TimeUnit timeUnit) {
    checkNotNull(timeUnit);
    return new Action.TimeOut(action, NANOSECONDS.convert(duration, timeUnit));
  }

  /**
   * Creates an action which retries given {@code action}.
   *
   * @param action   An action retried by the returned {@code Action}.
   * @param times    How many times given {@code action} will be retried. If 0 is given, no retry will happen.
   *                 If {@link com.github.dakusui.actionunit.Action.Retry#INFINITE} is given, returned
   *                 action will re-try infinitely until {@code action} successes.
   * @param interval Interval between actions.
   * @param timeUnit Time unit of {@code interval}.
   */
  public static Action retry(Action action, int times, long interval, TimeUnit timeUnit) {
    checkNotNull(timeUnit);
    return new Action.Retry(action, NANOSECONDS.convert(interval, timeUnit), times);
  }

  @SafeVarargs
  public static <T> Action forEach(Iterable<T> dataSource, Action.ForEach.Mode mode, Action action, Sink<T>... sinks) {
    return new Action.ForEach<>(
        mode.getFactory(),
        transform(dataSource, new ToSource<T>()),
        action,
        sinks
    );
  }

  @SafeVarargs
  public static <T> Action forEach(Iterable<T> dataSource, Action action, Sink<T>... sinks) {
    return forEach(dataSource, SEQUENTIALLY, action, sinks);
  }

  @SafeVarargs
  public static <T> Action forEach(Iterable<T> dataSource, Action.ForEach.Mode mode, final Sink<T>... sinks) {
    return forEach(
        dataSource,
        mode,
        sequential(
            transform(asList(sinks),
                new Function<Sink<T>, Action>() {
                  @Override
                  public Action apply(final Sink<T> sink) {
                    return tag(asList(sinks).indexOf(sink));
                  }
                }
            )),
        sinks);
  }

  @SafeVarargs
  public static <T> Action forEach(Iterable<T> dataSource, final Sink<T>... sinks) {
    return forEach(dataSource, SEQUENTIALLY, sinks);
  }

  public static Action tag(int i) {
    return new Action.With.Tag(i);
  }

  @SafeVarargs
  public static <T> Action with(T value, Action action, Sink<T>... sinks) {
    return new Action.With.Base<>(Connectors.immutable(value), action, sinks);
  }

  @SafeVarargs
  public static <T> Action with(T value, final Sink<T>... sinks) {
    return with(
        value,
        sequential(
            transform(asList(sinks),
                new Function<Sink<T>, Action>() {
                  @Override
                  public Action apply(final Sink<T> sink) {
                    return tag(asList(sinks).indexOf(sink));
                  }
                }
            )),
        sinks);
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
  public static Action waitFor(final long duration, final TimeUnit timeUnit) {
    checkArgument(duration >= 0, "duration must be non-negative but %d was given", duration);
    checkNotNull(timeUnit);
    return new Action.Leaf() {
      @Override
      public void perform() {
        try {
          timeUnit.sleep(duration);
        } catch (InterruptedException e) {
          throw propagate(e);
        }
      }

      @Override
      public String toString() {
        return format("Wait for %s", Utils.formatDuration(NANOSECONDS.convert(duration, timeUnit)));
      }
    };
  }

  public static Action.Attempt.Builder attempt(Action attempt) {
    return new Action.Attempt.Builder(checkNotNull(attempt));
  }

  public static Action.Attempt.Builder attempt(Runnable attempt) {
    return attempt(simple(checkNotNull(attempt)));
  }

  @SafeVarargs
  public static <I, O> Action pipe(
      Source<I> source,
      Pipe<I, O> piped,
      Sink<O>... sinks
  ) {
    return Action.Piped.Factory.create(source, piped, sinks);
  }

  @SafeVarargs
  public static <I, O> Action pipe(
      Source<I> source,
      Function<I, O> pipe,
      Sink<O>... sinks
  ) {
    return pipe(source, toPipe(pipe), sinks);
  }

  @SafeVarargs
  public static <I, O> Action pipe(
      Pipe<I, O> pipe,
      Sink<O>... sinks
  ) {
    return pipe(Connectors.<I>context(), pipe, sinks);
  }

  @SafeVarargs
  public static <I, O> Action pipe(
      Function<I, O> func,
      Sink<O>... sinks
  ) {
    return pipe(toPipe(func), sinks);
  }

  public static <I, O> TestAction.Builder<I, O> test() {
    return test(null);
  }

  public static <I, O> TestAction.Builder<I, O> test(String name) {
    return new TestAction.Builder<>(name);
  }


  public static class ToSource<T> implements Function<T, Source<T>> {
    @Override
    public Source<T> apply(T t) {
      return toSource(t);
    }
  }
}
