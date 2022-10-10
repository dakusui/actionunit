package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.context.ContextConsumer;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.exceptions.ActionTimeOutException;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.ut.utils.TestUtils;
import com.github.dakusui.actionunit.utils.InternalUtils;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.github.dakusui.actionunit.core.ActionSupport.*;
import static com.github.dakusui.actionunit.ut.utils.TestUtils.createActionPerformer;
import static com.github.dakusui.crest.Crest.*;
import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.*;
import static java.util.stream.Collectors.toList;

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

  @Test(expected = ActionTimeOutException.class)
  public void issue92() {
    List<String> alphabets = new ArrayList<String>() {{
      this.add("a");
      this.add("b");
    }};

    Action action = parallel(alphabets.stream()
        .map(alphabet -> leaf(c -> {
          if (alphabet.equals("b")) {
            try {
              Thread.sleep(2000);
            } catch (InterruptedException e) {
              e.getMessage();
            }
          }
        }))
        .map(a -> timeout(a).in(1, MILLISECONDS))
        .map(a -> retry(a)
            .on(ActionException.class)
            .withIntervalOf(1, MILLISECONDS)
            .times(2)
            .$())
        .collect(toList()));

    try {
      ReportingActionPerformer.create().performAndReport(action, Writer.Slf4J.INFO);
    } catch (ActionTimeOutException e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
      assertThat(
          e.getMessage(),
          asString()
              .containsString("timeout")
              .containsString("with message:")
              .$());
      throw e;
    }
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
