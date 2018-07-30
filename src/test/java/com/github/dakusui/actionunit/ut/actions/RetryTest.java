package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.compat.utils.TestUtils;
import com.github.dakusui.actionunit.actions.Retry;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.ContextConsumer;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import static com.github.dakusui.actionunit.compat.utils.TestUtils.hasItemAt;
import static com.github.dakusui.actionunit.core.ActionSupport.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsEqual.equalTo;

public class RetryTest extends TestUtils.TestBase {
  @Rule
  public TestName testName = new TestName();

  @Test(expected = IllegalArgumentException.class)
  public void givenNegativeInterval$whenCreated$thenExceptionThrown() {
    new Retry.Builder(nop())
        .on(ActionException.class)
        .withIntervalOf(-1 /* this is not valid */, SECONDS)
        .times(1)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void givenNegativeTimes$whenCreated$thenExceptionThrown() {
    new Retry.Builder(nop())
        .on(ActionException.class)
        .withIntervalOf(1, SECONDS)
        .times(-100 /* this is not valid */)
        .build();
  }

  @Test(expected = RuntimeException.class)
  public void given0AsTimes$whenActionFails$thenRetryNotAttempted() {
    // Make sure if 0 is given as retries, action will immediately quit.
    new Retry.Builder(actionFailOnce())
        .on(RuntimeException.class)
        .withIntervalOf(1, SECONDS)
        .times(0)
        .build()
        .accept(TestUtils.createActionPerformer());
  }

  @Test
  public void givenRetryOnNpe$whenNpeThrown$thenRetriedAndPassed() {
    TestUtils.Out outForRun = new TestUtils.Out();
    TestUtils.Out outForTree = new TestUtils.Out();
    Action action = composeRetryAction(outForRun, NullPointerException.class, new NullPointerException("HelloNpe"));
    try {
      ReportingActionPerformer.create(
          outForTree
      ).performAndReport(action);
    } finally {
      assertThat(
          outForTree,
          allOf(
              hasItemAt(0, equalTo("[.]1-Retry(1[milliseconds]x2times)")),
              hasItemAt(1, equalTo("  [EE.]0-Passes on third try"))
          ));
      assertThat(
          outForTree,
          hasSize(2));
      assertThat(
          outForRun,
          allOf(
              hasItemAt(0, equalTo("Throwing:HelloNpe")),
              hasItemAt(1, equalTo("Tried:0")),
              hasItemAt(2, equalTo("Throwing:HelloNpe")),
              hasItemAt(3, equalTo("Tried:1")),
              hasItemAt(4, equalTo("Passed")),
              hasItemAt(5, equalTo("Tried:2"))
          ));
      assertThat(
          outForRun,
          hasSize(6));
    }
  }

  @Test
  public void givenRetryOnActionException$whenActionExceptionThrown$thenRetriedAndPassed() {
    TestUtils.Out outForRun = new TestUtils.Out();
    TestUtils.Out outForTree = new TestUtils.Out();
    Action action = composeRetryAction(outForRun, ActionException.class, new ActionException("HelloException"));
    try {
      ReportingActionPerformer.create(Writer.Std.OUT).performAndReport(action);
    } finally {
      assertThat(
          outForTree,
          allOf(
              hasItemAt(0,
                  allOf(
                      containsString("[.]"),
                      containsString("Retry(1[milliseconds]x2times)")
                  )),
              hasItemAt(1,
                  allOf(
                      containsString("[EE.]"),
                      containsString("Passes on third try")
                  ))
          ));
      assertThat(
          outForTree,
          hasSize(2));
      assertThat(
          outForRun,
          allOf(
              hasItemAt(0, equalTo("Throwing:HelloException")),
              hasItemAt(1, equalTo("Tried:0")),
              hasItemAt(2, equalTo("Throwing:HelloException")),
              hasItemAt(3, equalTo("Tried:1")),
              hasItemAt(4, equalTo("Passed")),
              hasItemAt(5, equalTo("Tried:2"))
          ));
      assertThat(
          outForRun,
          hasSize(6));
    }
  }

  private <T extends Throwable, U extends RuntimeException> Action composeRetryAction(final TestUtils.Out out, Class<T> exceptionToBeCaught, final U exceptionToBeThrown) {
    return retry(
        simple(
            "Passes on third try",
            new ContextConsumer() {
              int tried = 0;

              @Override
              public void accept(Context context) {
                try {
                  if (tried < 2) {
                    out.writeLine("Throwing:" + exceptionToBeThrown.getMessage());
                    exceptionToBeThrown.fillInStackTrace();
                    throw exceptionToBeThrown;
                  }
                  out.writeLine("Passed");
                } finally {
                  out.writeLine("Tried:" + tried);
                  tried++;
                }
              }
            })
    ).on(
        exceptionToBeCaught
    ).times(
        2
    ).withIntervalOf(
        1, MILLISECONDS
    ).build();
  }

  private Action actionFailOnce() {
    return simple("Fail on first time only", new ContextConsumer() {
      boolean firstTime = true;

      @Override
      public void accept(Context context) {
        try {
          if (firstTime) {
            throw new RuntimeException(testName.getMethodName());
          }
        } finally {
          firstTime = false;
        }
      }
    });
  }
}
