package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.context.ContextConsumer;
import com.github.dakusui.processstreamer.core.process.ProcessStreamer;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.github.dakusui.processstreamer.core.process.ProcessStreamer.Checker.createCheckerForExitCode;
import static java.util.Objects.requireNonNull;

public interface Commander<C extends Commander<?>> {
  class RetryOption {
    public static RetryOption timeoutInSeconds(long timeoutInSeconds) {
      return new RetryOption(
          timeoutInSeconds,
          TimeUnit.SECONDS,
          Throwable.class,
          0,
          TimeUnit.SECONDS,
          0);
    }

    final long timeoutDuration;
    final TimeUnit timeoutTimeUnit;
    final Class<? extends Throwable> retryOn;
    final long retryInterval;
    final TimeUnit retryIntervalTimeUnit;
    final int retries;


    RetryOption(
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
  }

  C env(String varname, String varvalue);

  C stdin(Stream<String> stream);

  default Action toAction() {
    return toActionWith(createCheckerForExitCode(0));
  }

  Action toActionWith(ProcessStreamer.Checker checker);

  default ContextConsumer toContextConsumer() {
    return toContextConsumerWith(createCheckerForExitCode(0));
  }

  ContextConsumer toContextConsumerWith(ProcessStreamer.Checker checker);

}
