package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.ut.utils.TestUtils;
import org.junit.Test;

import static com.github.dakusui.actionunit.core.ActionSupport.attempt;
import static com.github.dakusui.actionunit.core.ActionSupport.sequential;
import static com.github.dakusui.actionunit.core.ActionSupport.simple;

public class AttemptExample extends TestUtils.TestBase {
  @Test(expected = IllegalArgumentException.class)
  public void givenAttemptAction$whenPerform$thenWorksFine() {
    buildAttemptAction().accept(TestUtils.createActionPerformer());
  }

  @Test(expected = IllegalArgumentException.class)
  public void givenAttemptAction$whenPerformAndReport$thenWorksFine() {
    TestUtils.createReportingActionPerformer().performAndReport(buildAttemptAction(), Writer.Std.OUT);
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
