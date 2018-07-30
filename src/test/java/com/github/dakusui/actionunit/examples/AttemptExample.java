package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.compat.utils.TestUtils;
import com.github.dakusui.actionunit.core.Action;
import org.junit.Test;

import static com.github.dakusui.actionunit.core.ActionSupport.*;

public class AttemptExample extends TestUtils.TestBase {
  @Test(expected = IllegalArgumentException.class)
  public void givenAttemptAction$whenPerform$thenWorksFine() {
    buildAttemptAction().accept(TestUtils.createActionPerformer());
  }

  @Test(expected = IllegalArgumentException.class)
  public void givenAttemptAction$whenPerformAndReport$thenWorksFine() {
    TestUtils.createReportingActionPerformer().performAndReport(buildAttemptAction());
  }


  private Action buildAttemptAction() {
    return attempt(
        sequential(
            simple("print hello", (c) -> System.out.println("Hello 'attempt'")),
            simple("throw exception", (c) -> {
                  throw new IllegalArgumentException();
                }
            ))
    ).recover(
        NullPointerException.class,
        simple(
            "print stacktrace",
            (c) -> {
            })
    ).ensure(
        simple(
            "print bye",
            (c) -> System.out.println("Bye 'attempt'"))
    );
  }
}
