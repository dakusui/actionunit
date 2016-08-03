package com.github.dakusui.actionunit;

import com.google.common.base.Preconditions;

import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;

/**
 * A utility class of ActionUnit framework.
 */
public enum Actions {
  ;

  public static Action simple(final Runnable runnable) {
    return simple(null, runnable);
  }

  public static Action simple(final String summary, final Runnable runnable) {
    Preconditions.checkNotNull(runnable);
    return new Action.Leaf() {
      @Override
      public String describe() {
        return summary == null
            ? "(noname)"
            : summary;
      }

      @Override
      public void perform() {
        runnable.run();
      }
    };
  }

  public static Action concurrent(Action... actions) {
    return concurrent(null, actions);
  }

  public static Action concurrent(String summary, Action... actions) {
    return Action.Concurrent.Factory.INSTANCE.create(summary, asList(actions));
  }

  public static Action sequential(Action... actions) {
    return sequential(null, actions);
  }

  public static Action sequential(String summary, Action... actions) {
    return Action.Sequential.Factory.INSTANCE.create(summary, asList(actions));
  }

  public static Action timeout(Action action, int duration, TimeUnit timeUnit) {
    Preconditions.checkNotNull(timeUnit);
    return new Action.TimeOut(action, TimeUnit.NANOSECONDS.convert(duration, timeUnit));
  }

  public static Action retry(Action action, int times, int interval, TimeUnit timeUnit) {
    Preconditions.checkNotNull(timeUnit);
    return new Action.Retry(action, TimeUnit.NANOSECONDS.convert(interval, timeUnit), times);
  }

  public static <T> Action repeatIncrementally(
      Action.WithTarget.Factory<T> factoryForActionWithTarget,
      Iterable<T> datasource) {
    return new Action.RepeatIncrementally<>(datasource, factoryForActionWithTarget);
  }
}
