package com.github.dakusui.actionunit;

import java.util.concurrent.TimeUnit;

import static com.github.dakusui.actionunit.Utils.nonameIfNull;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
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
    checkNotNull(timeUnit);
    return new Action.TimeOut(action, TimeUnit.NANOSECONDS.convert(duration, timeUnit));
  }

  public static Action retry(Action action, int times, int interval, TimeUnit timeUnit) {
    checkNotNull(timeUnit);
    return new Action.Retry(action, TimeUnit.NANOSECONDS.convert(interval, timeUnit), times);
  }

  public static <T> Action repeatIncrementally(
      Iterable<T> datasource, Action.WithTarget.Factory<T> factoryForActionWithTarget) {
    return new Action.RepeatIncrementally<>(datasource, factoryForActionWithTarget);
  }

  public static <T> Action.WithTarget.Factory<T> forEach(final Block<T> f) {
    return forEach(null, f);
  }

  public static <T> Action.WithTarget.Factory<T> forEach(final String summary, final Block<T> f) {
    checkNotNull(f);
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
            f.apply(target);
          }
        };
      }

      @Override
      public String describe() {
        return nonameIfNull(summary);
      }
    };
  }

  public static Action nop() {
    return new Action.Leaf() {
      @Override
      public void perform() {
      }

      @Override
      public String describe() {
        return "nop";
      }
    };
  }

}
