package com.github.dakusui.actionunit.compat;

import com.github.dakusui.actionunit.actions.ForEach;
import com.github.dakusui.actionunit.actions.Leaf;
import com.github.dakusui.actionunit.actions.Retry;
import com.github.dakusui.actionunit.actions.Sequential;
import com.github.dakusui.actionunit.compat.actions.*;
import com.github.dakusui.actionunit.compat.connectors.Connectors;
import com.github.dakusui.actionunit.compat.connectors.Pipe;
import com.github.dakusui.actionunit.compat.connectors.Sink;
import com.github.dakusui.actionunit.compat.connectors.Source;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.DataSource;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.helpers.Actions;
import com.github.dakusui.actionunit.helpers.Builders;
import com.github.dakusui.actionunit.helpers.Utils;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.dakusui.actionunit.actions.ForEach.Mode.SEQUENTIALLY;
import static com.github.dakusui.actionunit.compat.connectors.Connectors.toPipe;
import static com.github.dakusui.actionunit.compat.connectors.Connectors.toSource;
import static com.github.dakusui.actionunit.helpers.Checks.checkNotNull;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

@Deprecated
public enum CompatActions {
  ;

  @SafeVarargs
  public static <T> Action foreach(DataSource.Factory<T> dataSourceFactory, ForEach.Mode mode, Action action, Sink<T>... sinks) {
    return new CompatForEach<>(
        mode.getFactory(),
        new DataSource.Factory.Adapter<>(dataSourceFactory, ToSource.instance()),
        action,
        sinks
    );
  }

  @SafeVarargs
  public static <T> Action foreach(Iterable<T> dataSource, ForEach.Mode mode, Action action, Sink<T>... sinks) {
    return foreach(new DataSource.Factory.PassThrough<>(dataSource), mode, action, sinks);
  }

  @SafeVarargs
  public static <T> Action foreach(Iterable<T> dataSource, Action action, Sink<T>... sinks) {
    return foreach(dataSource, SEQUENTIALLY, action, sinks);
  }

  @SafeVarargs
  public static <T> Action foreach(Iterable<T> dataSource, ForEach.Mode mode, final Sink<T>... sinks) {
    return foreach(
        dataSource,
        mode,
        Actions.sequential(
            Arrays.stream(sinks).map(sink -> tag(asList(sinks).indexOf(sink))).collect(toList())
        ),
        sinks);
  }

  @SafeVarargs
  public static <T> Action foreach(Iterable<T> dataSource, final Sink<T>... sinks) {
    return foreach(dataSource, SEQUENTIALLY, sinks);
  }

  @SafeVarargs
  public static <T> Action with(T value, Action action, Sink<T>... sinks) {
    return new CompatWithBase<>(Connectors.immutable(value), action, sinks);
  }

  @SafeVarargs
  public static <T> Action with(T value, final Sink<T>... sinks) {
    return with(
        value,
        Actions.sequential(
            Arrays.stream(sinks).map(sink -> tag(asList(sinks).indexOf(sink))).collect(toList())
        ),
        sinks);
  }

  public static CompatAttempt.Builder attempt(Action attempt) {
    return new CompatAttempt.Builder(checkNotNull(attempt));
  }

  public static CompatAttempt.Builder attempt(Runnable attempt) {
    return attempt(simple(checkNotNull(attempt)));
  }

  @SafeVarargs
  public static <I, O> Action pipe(
      Source<I> source,
      Pipe<I, O> piped,
      Sink<O>... sinks
  ) {
    return Piped.Factory.create(source, piped, sinks);
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

  public static <I, O> CompatTestAction.Builder<I, O> test() {
    return test(null);
  }

  public static <I, O> CompatTestAction.Builder<I, O> test(String name) {
    return new CompatTestAction.Builder<>(name);
  }

  public static <I> Action sink(String description, final Sink<I> sink) {
    return pipe(toPipe(description, sink));
  }

  public static <I> Action sink(final Sink<I> sink) {
    return sink(null, sink);
  }

  public static Action tag(int i) {
    return new Tag(i);
  }

  /**
   * Creates a simple action object.
   *
   * @param runnable An object whose {@code run()} method run by a returned {@code Action} object.
   * @see Leaf
   */
  public static Action simple(final Runnable runnable) {
    return createLeafAction(runnable);
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
    return Actions.named(summary, Actions.concurrent(actions));
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

  @Deprecated
  public static Action createLeafAction(Runnable runnable) {
    return Leaf.create(Utils.describe(runnable), runnable);
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
    /*
    checkNotNull(timeUnit);
    //noinspection unchecked
    return new Retry(targetExceptionClass, action, NANOSECONDS.convert(interval, timeUnit), times);
    */
    return Builders.retry(action)
        .times(times)
        .on(targetExceptionClass)
        .withIntervalOf(interval, timeUnit);
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

  /**
   * Creates an action which runs given {@code actions} in a sequential manner.
   *
   * @param summary A string used by {@code describe()} method of a returned {@code Action} object.
   * @param actions {@code Action} objects performed by returned {@code Action} object.
   * @see Sequential.Impl
   */
  public static Action sequential(String summary, Iterable<? extends Action> actions) {
    return Actions.named(summary, Actions.sequential(actions));
  }

  public static Action when(Predicate<?> condition, Action action, Action otherwise) {
    //return new When.Impl(condition, action, otherwise);
    return Builders.when(condition)
        .perform(action)
        .otherwise(otherwise)
        .build();
  }

  public static Action when(Predicate<?> condition, Action action) {
    return when(condition, action, Actions.nop());
  }

  public static <T> Action repeatwhile(Predicate<T> condition, Action... actions) {
    Action action = Actions.nop();
    if (actions.length == 1) {
      action = actions[0];
    } else if (actions.length > 1) {
      action = Actions.sequential(actions);
    }
    return Builders.loopWhile(condition).perform(action);
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
    return Builders.timeout(action).in(duration, timeUnit);
  }

  public static class ToSource<T> implements Function<T, Source<T>> {
    private static final ToSource<?> INSTANCE = new ToSource<>();

    public static <T> ToSource<T> instance() {
      //noinspection unchecked
      return (ToSource<T>) INSTANCE;
    }

    ToSource() {
    }

    @Override
    public Source<T> apply(T t) {
      return toSource(t);
    }
  }
}
