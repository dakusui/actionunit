package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.compat.utils.TestUtils;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.utils.InternalUtils;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static com.github.dakusui.actionunit.compat.utils.TestUtils.createActionPerformer;
import static com.github.dakusui.actionunit.core.ActionSupport.nop;
import static com.github.dakusui.actionunit.core.ActionSupport.timeout;
import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class TimeoutTest extends TestUtils.TestBase {
  @Test(expected = IllegalArgumentException.class)
  public void givenNegativeDuration$whenCreated$thenExceptionThrown() {
    timeout(nop()).in(-2, SECONDS);
  }

  @Test(expected = InterruptedException.class)
  public void whenInterrupted() throws Throwable {
    final Thread main = currentThread();
    Thread interrupter = new Thread(() -> {
      InternalUtils.sleep(500, MILLISECONDS);
      main.interrupt();
    });
    Action action = timeout(
        TestUtils.sleep(5, SECONDS)
    ).in(
        10, SECONDS
    );
    interrupter.start();
    try {
      action.accept(createActionPerformer());
    } catch (ActionException e) {
      throw e.getCause();
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void givenNonPositive$whenCreateTimeoutAction$thenIllegalArgument() {
    timeout(nop()).in(0, TimeUnit.SECONDS);
  }
}
