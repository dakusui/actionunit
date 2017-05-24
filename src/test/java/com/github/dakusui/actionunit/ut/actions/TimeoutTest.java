package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.compat.CompatActions;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.helpers.Builders;
import com.github.dakusui.actionunit.helpers.Utils;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static com.github.dakusui.actionunit.helpers.Actions.*;
import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class TimeoutTest {
  @Test(expected = IllegalArgumentException.class)
  public void givenNegativeDuration$whenCreated$thenExceptionThrown() {
    Builders.timeout(nop()).in(-2, SECONDS);
  }

  @Test(expected = InterruptedException.class)
  public void whenInterrupted() throws Throwable {
    final Thread main = currentThread();
    Thread interrupter = new Thread(new Runnable() {
      @Override
      public void run() {
        Utils.sleep(500, MILLISECONDS);
        main.interrupt();
      }
    });
    Action action = CompatActions.timeout(
        sleep(5, SECONDS),
        10, SECONDS
    );
    interrupter.start();
    try {
      action.accept(new ActionRunner.Impl());
    } catch (ActionException e) {
      throw e.getCause();
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void givenNonPositive$whenCreateTimeoutAction$thenIllegalArgument() {
    CompatActions.timeout(nop(), 0, TimeUnit.SECONDS);
  }
}
