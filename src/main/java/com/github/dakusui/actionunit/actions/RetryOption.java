package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import static com.github.dakusui.actionunit.core.ActionSupport.retry;
import static com.github.dakusui.actionunit.core.ActionSupport.timeout;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.SECONDS;

public class RetryOption implements Serializable {
  public static class Builder {
    long                       timeoutDuration       = -1;
    TimeUnit                   timeoutTimeUnit       = SECONDS;
    Class<? extends Throwable> retryOn               = Throwable.class;
    long                       retryInterval         = -1;
    TimeUnit                   retryIntervalTimeUnit = SECONDS;
    int                        retries               = -1;

    public Builder timeoutIn(long timeoutDuration, TimeUnit timeoutTimeUnit) {
      this.timeoutDuration = timeoutDuration;
      this.timeoutTimeUnit = timeoutTimeUnit;
      return this;
    }

    public Builder retryOn(Class<? extends Throwable> retryOn) {
      this.retryOn = requireNonNull(retryOn);
      return this;
    }

    public Builder retries(int retries) {
      this.retries = retries;
      return this;
    }

    public Builder retryInterval(long retryInterval, TimeUnit retryIntervalTimeUnit) {
      this.retryInterval = retryInterval;
      this.retryIntervalTimeUnit = retryIntervalTimeUnit;
      return this;
    }

    public RetryOption build() {
      return new RetryOption(
          timeoutDuration,
          timeoutTimeUnit,
          retryOn,
          retryInterval,
          retryIntervalTimeUnit,
          retries);
    }
  }

  public static RetryOption.Builder builder() {
    return new RetryOption.Builder();
  }

  public static RetryOption timeoutInSeconds(long timeoutInSeconds) {
    return new RetryOption.Builder()
        .timeoutIn(timeoutInSeconds, SECONDS)
        .retries(0)
        .retryOn(Throwable.class)
        .retryInterval(0, SECONDS)
        .build();
  }

  public static RetryOption none() {
    return new Builder()
        .timeoutIn(-1, SECONDS)
        .retryOn(Throwable.class)
        .timeoutIn(-1, SECONDS)
        .retries(-1).build();
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
