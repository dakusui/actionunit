package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.actions.Attempt;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.utils.TestUtils;
import org.junit.Test;

import static java.lang.String.format;

public class AttemptExample implements Context {
  @Test(expected = IllegalArgumentException.class)
  public void givenAttemptAction$whenPerform$thenWorksFine() {
    buildAttemptAction().accept(TestUtils.createActionPerformer());
  }

  @Test
  public void givenAttemptAction$whenPrint$thenWorksFine() {
    buildAttemptAction().accept(TestUtils.createPrintingActionScanner(Writer.Std.OUT));
  }

  @Test(expected = IllegalArgumentException.class)
  public void givenAttemptAction$whenPerformAndReport$thenWorksFine() {
    TestUtils.createReportingActionPerformer(buildAttemptAction()).performAndReport();
  }


  private Attempt<? super NullPointerException> buildAttemptAction() {
    return attempt(
        sequential(
            simple("print hello", () -> System.out.println("Hello 'attempt'")),
            simple("throw exception", () -> {
                  throw new IllegalArgumentException();
                }
            ))
    ).recover(
        NullPointerException.class,
        ($, e) -> $.simple(
            format("print stacktrace:<%s>", e),
            () -> {
            })
    ).ensure(
        $ -> $.simple(
            "print bye",
            () -> System.out.println("Bye 'attempt'"))
    );
  }
}
