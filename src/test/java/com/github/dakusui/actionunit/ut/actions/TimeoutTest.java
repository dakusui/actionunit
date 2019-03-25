package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.context.ContextConsumer;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.exceptions.ActionTimeOutException;
import com.github.dakusui.actionunit.ut.utils.TestUtils;
import com.github.dakusui.actionunit.utils.InternalUtils;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static com.github.dakusui.actionunit.core.ActionSupport.nop;
import static com.github.dakusui.actionunit.core.ActionSupport.parallel;
import static com.github.dakusui.actionunit.core.ActionSupport.retry;
import static com.github.dakusui.actionunit.core.ActionSupport.sequential;
import static com.github.dakusui.actionunit.core.ActionSupport.simple;
import static com.github.dakusui.actionunit.core.ActionSupport.timeout;
import static com.github.dakusui.actionunit.ut.utils.TestUtils.createActionPerformer;
import static com.github.dakusui.crest.Crest.allOf;
import static com.github.dakusui.crest.Crest.asInteger;
import static com.github.dakusui.crest.Crest.asString;
import static com.github.dakusui.crest.Crest.assertThat;
import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
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

  @Test(expected = ActionTimeOutException.class)
  public void givenParallelActionUnderTimeOut() {
    Action action = timeout(
        parallel(
            sequential(
                sleepAction(100),
                print("hello1")
            ),
            sequential(
                sleepAction(10),
                print("hello2")
            )
        )
    ).in(50, MILLISECONDS);

    try {
      action.accept(createActionPerformer());
    } catch (ActionTimeOutException e) {
      assertThat(
          out,
          allOf(
              asString("get", 0).equalTo("hello2").$(),
              asInteger("size").equalTo(1).$()
          )
      );
      throw e;
    }
  }

  @Test(timeout = 2_000, expected = ActionTimeOutException.class)
  public void givenNotEndingRetryActionInside$whenTimeoutOutside$thenTimeoutsAppropriately() {
    Action action = timeout(
        retry(
            simple("throw an Exception", c -> {
              throw new RuntimeException();
            })
        ).times(Integer.MAX_VALUE)
            .on(Exception.class).withIntervalOf(10, MILLISECONDS).$()
    ).in(1, SECONDS);
    action.accept(createActionPerformer());
  }

  @Test(timeout = 2_000, expected = ActionTimeOutException.class)
  public void givenLongerTimeoutActionWhichHoldsNotEndingRetryAction$whenTimeoutOutside$thenOuterTimeoutUsed() {
    Action action = timeout(
        timeout(
        retry(
            simple("throw an Exception", c -> {
              throw new RuntimeException();
            })
        ).times(Integer.MAX_VALUE)
            .on(Exception.class).withIntervalOf(10, MILLISECONDS).$()
        ).in(60, MINUTES)
    ).in(1, SECONDS);
    action.accept(createActionPerformer());
  }

  @Test(timeout = 2_000, expected = ActionTimeOutException.class)
  public void givenShorterTimeoutActionWhichHoldsNotEndingRetryAction$whenTimeoutOutside$thenInnerTimeoutUsed() {
    Action action = timeout(
        timeout(
            retry(
                simple("throw an Exception", c -> {
                  throw new RuntimeException();
                })
            ).times(Integer.MAX_VALUE)
                .on(Exception.class).withIntervalOf(10, MILLISECONDS).$()
        ).in(100, MILLISECONDS)
    ).in(1, MINUTES);
    action.accept(createActionPerformer());
  }

  private Action sleepAction(long millis) {
    return simple(String.format("%d[millis]", millis), new ContextConsumer() {
      @Override
      public void accept(Context context) {
        try {
          Thread.sleep(millis);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw ActionException.wrap(e);
        }
      }
    });
  }

  private Action print(String message) {
    return simple(String.format("print:'%s'", message),
        context -> printf(message)
    );
  }
}
