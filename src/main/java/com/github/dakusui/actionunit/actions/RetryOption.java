package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;

import java.util.concurrent.TimeUnit;

import static com.github.dakusui.actionunit.core.ActionSupport.retry;
import static com.github.dakusui.actionunit.core.ActionSupport.timeout;
import static java.util.Objects.requireNonNull;

public class RetryOption {
  public static RetryOption timeoutInSeconds(long timeoutInSeconds) {
    return new RetryOption(
        timeoutInSeconds,
        TimeUnit.SECONDS,
        Throwable.class,
        0,
        TimeUnit.SECONDS,
        0);
  }

  public static RetryOption none() {
    return new RetryOption(
        -1,
        TimeUnit.SECONDS,
        Throwable.class,
        -1,
        TimeUnit.SECONDS,
        -1
    );
  }

  public final long                       timeoutDuration;
  public final TimeUnit                   timeoutTimeUnit;
  public final Class<? extends Throwable> retryOn;
  public final long                       retryInterval;
  public final TimeUnit                   retryIntervalTimeUnit;
  public final int                        retries;


  private RetryOption(
      long timeoutDuration,
      TimeUnit timeoutTimeUnit,
      Class<? extends Throwable> retryOn,
      long retryInterval,
      TimeUnit retryIntervalTimeUnit,
      int retries) {
    this.timeoutDuration = timeoutDuration;
    this.timeoutTimeUnit = requireNonNull(timeoutTimeUnit);
    this.retryOn = requireNonNull(retryOn);
    this.retryInterval = retryInterval;
    this.retryIntervalTimeUnit = requireNonNull(retryIntervalTimeUnit);
    this.retries = retries;
  }

  public static Action retryAndTimeOut(Action action, RetryOption retryOption) {
    if (retryOption.retries > 0)
      action = retry(action)
          .on(retryOption.retryOn)
          .times(retryOption.retries)
          .withIntervalOf(retryOption.retryInterval, retryOption.retryIntervalTimeUnit)
          .$();
    if (retryOption.timeoutDuration >= 0)
      action = timeout(action).in(retryOption.timeoutDuration, retryOption.timeoutTimeUnit);
    return action;
  }
}
