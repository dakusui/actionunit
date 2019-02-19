package com.github.dakusui.actionunit.extras.cmd;

import com.github.dakusui.actionunit.actions.cmd.Commander;
import com.github.dakusui.processstreamer.core.process.ProcessStreamer;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class RetryTest<R extends Commander<R>> extends FsTestBase<R> {
  public RetryTest() throws IOException {
  }

  @SuppressWarnings("unchecked")
  @Override
  protected R create() {
    long start = System.currentTimeMillis() / 1000;
    return (R) new Commander<R>() {
      @Override
      protected String program() {
        return "if [[ $(($(date +'%s') - " + start + ")) -ge 3 ]]; then echo success; else echo fail && exit 1;fi";
      }
    };
  }


  @Test(expected = ProcessStreamer.Failure.class)
  public void whenRetryOnce$thenFail() {
    perform(this.commander.retries(1).interval(1, TimeUnit.SECONDS).build());
  }

  @Test
  public void whenRetryThreeTimes$thenPass() {
    perform(this.commander.retries(3).interval(1, TimeUnit.SECONDS).build());
  }
}
