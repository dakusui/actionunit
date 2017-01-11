package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.Context;
import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.actionunit.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import org.junit.Test;

import static com.github.dakusui.actionunit.Actions.*;
import static com.github.dakusui.actionunit.utils.TestUtils.hasItemAt;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.junit.MatcherAssert.assertThat;

public class AttemptTest {
  @Test
  public void givenAttemptContainingRetryInsideRecover$whenExceptionThrown$thenRetryIsDoneExpectedly() {
    Action action =
        attempt(named("Fail",
            simple(new Runnable() {
              @Override
              public void run() {
                throw new RuntimeException("ThrowException");
              }
            }))
        ).recover(
            RuntimeException.class,
            retry(RuntimeException.class,
                tag(0),
                1,
                1, MILLISECONDS),
            new Sink<RuntimeException>() {
              boolean tried = false;

              @Override
              public void apply(RuntimeException input, Context context) {
                try {
                  if (!tried) {
                    throw new NullPointerException("FirstTime:" + input.getMessage());
                  }
                } finally {
                  tried = true;
                }
              }
            }
        ).build();
    ActionRunner.WithResult runner = new ActionRunner.WithResult();
    try {
      action.accept(runner);
    } finally {
      TestUtils.Out outForTree = new TestUtils.Out();
      action.accept(runner.createPrinter(outForTree));
      assertThat(
          outForTree,
          allOf(
              hasItemAt(0, equalTo("(+)Attempt")),
              hasItemAt(1, equalTo("  (E)Fail(error=ThrowException)")),
              hasItemAt(2, equalTo("    (E)AttemptTest$2(error=ThrowException)")),
              hasItemAt(3, equalTo("  (+)Recover")),
              hasItemAt(4, equalTo("    (+)Retry(1[milliseconds]x1times)")),
              hasItemAt(5, equalTo("      (+)Tag(0); 2 times")),
              hasItemAt(6, equalTo("  (+)Ensure")),
              hasItemAt(7, equalTo("    (+)(nop)"))));
      assertThat(
          outForTree,
          hasSize(8));

    }
  }
}