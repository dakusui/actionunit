package com.github.dakusui.actionunit.compat;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.Actions;
import com.github.dakusui.actionunit.DataSource;
import com.github.dakusui.actionunit.actions.ForEach;
import com.github.dakusui.actionunit.compat.actions.Tag;
import com.github.dakusui.actionunit.compat.actions.*;
import com.github.dakusui.actionunit.compat.connectors.Connectors;
import com.github.dakusui.actionunit.compat.connectors.Pipe;
import com.github.dakusui.actionunit.compat.connectors.Sink;
import com.github.dakusui.actionunit.compat.connectors.Source;

import java.util.Arrays;
import java.util.function.Function;

import static com.github.dakusui.actionunit.Checks.checkNotNull;
import static com.github.dakusui.actionunit.actions.ForEach.Mode.SEQUENTIALLY;
import static com.github.dakusui.actionunit.compat.connectors.Connectors.toPipe;
import static com.github.dakusui.actionunit.compat.connectors.Connectors.toSource;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

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
    return attempt(Actions.simple(checkNotNull(attempt)));
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
