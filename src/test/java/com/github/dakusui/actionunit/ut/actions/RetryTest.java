package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.actions.Retry;
import com.github.dakusui.actionunit.exceptions.Abort;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import org.junit.Test;

import static com.github.dakusui.actionunit.Actions.*;
import static com.github.dakusui.actionunit.utils.TestUtils.hasItemAt;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsEqual.equalTo;

public class RetryTest {
  @Test(expected = IllegalArgumentException.class)
  public void givenNegativeInterval$whenCreated$thenExceptionThrown() {
    new Retry(ActionException.class, nop(), -1 /* this is not valid */, 1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void givenNegativeTimes$whenCreated$thenExceptionThrown() {
    new Retry(ActionException.class, nop(), 1, -100 /* this is not valid*/);
  }

  @Test(expected = IllegalArgumentException.class)
  public void givenAbort$whenCreated$thenExceptionThrown() {
    new Retry(Abort.class  /* this is not valid*/, nop(), 1, 1);
  }

  @Test
  public void givenFOREVERAsTimes$whenCreated$thenExceptionNotThrown() {
    // Make sure only an exception is not thrown on instantiation.
    new Retry(ActionException.class, nop(), 1, Retry.INFINITE);
  }

  @Test
  public void givenRetryOnNpe$whenNpeThrown$thenRetriedAndPassed() {
    TestUtils.Out outForRun = new TestUtils.Out();
    Action action = composeRetryAction(outForRun, NullPointerException.class, new NullPointerException("HelloNpe"));
    ActionRunner.WithResult runner = new ActionRunner.WithResult();
    try {
      action.accept(runner);
    } finally {
      TestUtils.Out outForTree = new TestUtils.Out();
      action.accept(runner.createPrinter(outForTree));
      assertThat(
          outForTree,
          allOf(
              hasItemAt(0, equalTo("(+)Retry(1[milliseconds]x2times)")),
              hasItemAt(1, equalTo("  (+)PassOn2ndRetry; 3 times")),
              hasItemAt(2, equalTo("    (+)RetryTest$1; 3 times"))
          ));
      assertThat(
          outForTree,
          hasSize(3));
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
    Action action = composeRetryAction(outForRun, ActionException.class, new ActionException("HelloException"));
    ActionRunner.WithResult runner = new ActionRunner.WithResult();
    try {
      action.accept(runner);
    } finally {
      TestUtils.Out outForTree = new TestUtils.Out();
      action.accept(runner.createPrinter(outForTree));
      assertThat(
          outForTree,
          allOf(
              hasItemAt(0, equalTo("(+)Retry(1[milliseconds]x2times)")),
              hasItemAt(1, equalTo("  (+)PassOn2ndRetry; 3 times")),
              hasItemAt(2, equalTo("    (+)RetryTest$1; 3 times"))
          ));
      assertThat(
          outForTree,
          hasSize(3));
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
        exceptionToBeCaught,
        named("PassOn2ndRetry",
            simple(new Runnable() {
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
        ),
        2,
        1, MILLISECONDS
    );
  }
}
