package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.actions.Retry;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.ActionFactory;
import com.github.dakusui.actionunit.core.ActionSupport;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.reporting.ReportingActionPerformer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.util.ArrayList;
import java.util.List;

import static com.github.dakusui.actionunit.utils.TestUtils.hasItemAt;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsEqual.equalTo;

public class RetryTest extends TestUtils.TestBase implements ActionFactory {

  @Rule
  public TestName testName = new TestName();

  @Test(expected = IllegalArgumentException.class)
  public void givenNegativeInterval$whenCreated$thenExceptionThrown() {
    new Retry.Builder(nop())
        .on(ActionException.class)
        .times(1)
        .withIntervalOf(-1 /* this is not valid */, SECONDS);
  }

  @Test(expected = IllegalArgumentException.class)
  public void givenNegativeTimes$whenCreated$thenExceptionThrown() {
    new Retry.Builder(nop())
        .on(ActionException.class)
        .times(-100 /* this is not valid */)
        .withIntervalOf(1, SECONDS);
  }

  @Test
  public void givenFOREVERAsTimes$whenCreated$thenExceptionNotThrown() {
    // Make sure only an exception is not thrown on instantiation.
    new Retry.Builder(nop())
        .on(ActionException.class)
        .times(Retry.INFINITE)
        .withIntervalOf(1, SECONDS);
  }

  @Test(expected = RuntimeException.class)
  public void given0AsTimes$whenActionFails$thenRetryNotAttempted() {
    new Retry.Builder(actionFailOnce())
        .on(RuntimeException.class)
        .times(0)
        .withIntervalOf(1, SECONDS)
        .accept(TestUtils.createActionPerformer());
  }

  @Test
  public void givenExceptionCollector$whenActionFails$thenExceptionCollected() {
    List<Throwable> throwables = new ArrayList<>();

    new Retry.Builder(actionFailOnce())
        .on(RuntimeException.class)
        .times(10)
        .handler(throwables::add)
        .withIntervalOf(10, MILLISECONDS)
        .accept(TestUtils.createActionPerformer());

    assertThat(throwables, hasSize(1));
    assertThat(throwables.get(0).getMessage(), is(testName.getMethodName()));
  }

  @Test
  public void givenRetryOnNpe$whenNpeThrown$thenRetriedAndPassed() {
    TestUtils.Out outForRun = new TestUtils.Out();
    TestUtils.Out outForTree = new TestUtils.Out();
    Action action = composeRetryAction(outForRun, NullPointerException.class, new NullPointerException("HelloNpe"));
    try {
      new ReportingActionPerformer.Builder(action).to(outForTree).build().performAndReport();
    } finally {
      assertThat(
          outForTree,
          allOf(
              hasItemAt(0, equalTo("[o]Retry(1[milliseconds]x2times)")),
              hasItemAt(1, equalTo("  [xxo]Passes on third try"))
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
      new ReportingActionPerformer.Builder(action).to(outForTree).build().performAndReport();
    } finally {
      assertThat(
          outForTree,
          allOf(
              hasItemAt(0, equalTo("[o]Retry(1[milliseconds]x2times)")),
              hasItemAt(1, equalTo("  [xxo]Passes on third try"))
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
            new Runnable() {
              int tried = 0;

              @Override
              public void run() {
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
    );
  }

  private Action actionFailOnce() {
    return ActionSupport.simple("Fail on first time only", new Runnable() {
      boolean firstTime = true;

      @Override
      public void run() {
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
