package com.github.dakusui.actionunit.compat.actions;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.compat.CompatActions;
import com.github.dakusui.actionunit.compat.Context;
import com.github.dakusui.actionunit.compat.connectors.Sink;
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

public class CompatAttemptTest {
  @Test
  public void givenAttemptContainingRetryInsideRecover$whenExceptionThrown$thenRetryIsDoneExpectedly() {
    Action action =
        CompatActions.attempt(named("Fail",
            CompatActions.simple(new Runnable() {
              @Override
              public void run() {
                throw new RuntimeException("ThrowException");
              }
            }))
        ).recover(
            RuntimeException.class,
            retry(RuntimeException.class,
                CompatActions.tag(0),
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
              hasItemAt(0, equalTo("(+)CompatAttempt")),
              hasItemAt(1, equalTo("  (E)Fail")),
              hasItemAt(2, equalTo("    (E)CompatAttemptTest$2")),
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
