package com.github.dakusui.actionunit.utils;

import com.github.dakusui.actionunit.exceptions.ActionException;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static com.github.dakusui.actionunit.utils.Checks.checkNotNull;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public enum InternalUtils {
  ;

  public static final Pattern OBJECT_TO_STRING_PATTERN = Pattern.compile(
      "([a-zA-Z\\$_][a-zA-Z0-9\\$_]*\\.)*([a-zA-Z0-9\\$_]+)*(\\$[0-9a-f]+/[0-9a-f]+)?@[]0-9a-f]+"
  );

  public static void sleep(long duration, TimeUnit timeUnit) {
    try {
      checkNotNull(timeUnit).sleep(duration);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw ActionException.wrap(e);
    }
  }

  public static <T> T runWithTimeout(Callable<T> callable, long timeout, TimeUnit timeUnit) {
    final ExecutorService executor = Executors.newSingleThreadExecutor();
    final Future<T> future = executor.submit(callable);
    executor.shutdown(); // This does not cancel the already-scheduled task.
    try {
      Thread.interrupted();
      return future.get(timeout, timeUnit);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw ActionException.wrap(e);
    } catch (TimeoutException e) {
      future.cancel(true);
      throw ActionException.wrap(e);
    } catch (ExecutionException e) {
      //unwrap the root cause
      Throwable cause = e.getCause();
      if (cause instanceof Error) {
        throw (Error) cause;
      }
      ////
      // It's safe to directly cast to RuntimeException, because a Callable can only
      // throw an Error or a RuntimeException.
      throw (RuntimeException) cause;
    } finally {
      executor.shutdownNow();
    }
  }

  public static String indent(int level, int indentWidth) {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < level; i++)
      b.append(spaces(indentWidth));
    return b.toString();
  }

  public static String formatNumberOfTimes(int i) {
    if (i == 1)
      return "once";
    if (i == 2)
      return "twice";
    return String.format("%s times", i);
  }


  public static String formatDuration(long durationInNanos) {
    TimeUnit timeUnit = chooseTimeUnit(durationInNanos);
    return format("%d [%s]", timeUnit.convert(durationInNanos, TimeUnit.NANOSECONDS), timeUnit.toString().toLowerCase());
  }

  private static TimeUnit chooseTimeUnit(long intervalInNanos) {
    // TimeUnit.values() returns elements of TimeUnit in declared order
    // And they are declared in ascending order.
    for (TimeUnit timeUnit : TimeUnit.values()) {
      if (1000 > timeUnit.convert(intervalInNanos, TimeUnit.NANOSECONDS)) {
        return timeUnit;
      }
    }
    return TimeUnit.DAYS;
  }

  public static String spaces(int numSpaces) {
    StringBuilder ret = new StringBuilder();
    for (int i = 0; i < numSpaces; i++) {
      ret.append(" ");
    }
    return ret.toString();
  }

  public static String summary(String s) {
    return requireNonNull(s).length() > 40
        ? s.substring(0, 40) + "..."
        : s;
  }

  public static String objectToStringIfOverridden(Object o, Supplier<String> formatter) {
    String s = o.toString();
    requireNonNull(formatter);
    if (OBJECT_TO_STRING_PATTERN.matcher(s).matches())
      return formatter.get();
    return s;
  }

  public static <T, R> Function<T, R> memoize(Function<T, R> function) {
    Map<T, R> memo = new ConcurrentHashMap<>();
    return t -> memo.computeIfAbsent(t, function);
  }
}
