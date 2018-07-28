package com.github.dakusui.actionunit.n.utils;

import com.github.dakusui.actionunit.exceptions.ActionException;

import java.util.concurrent.*;

import static com.github.dakusui.actionunit.helpers.Checks.checkNotNull;

public enum InternalUtils {
  ;

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
}
