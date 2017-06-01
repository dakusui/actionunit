package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.actions.Attempt;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.utils.TestUtils;
import org.junit.Test;

import static com.github.dakusui.actionunit.core.ActionSupport.sequential;
import static com.github.dakusui.actionunit.core.ActionSupport.simple;
import static com.github.dakusui.actionunit.core.ActionSupport.attempt;

public class AttemptExample {
  @Test(expected = IllegalArgumentException.class)
  public void givenAttemptAction$whenPerform$thenWorksFine() {
    buildAttemptAction().accept(TestUtils.createActionPerformer());
  }

  @Test
  public void givenAttemptAction$whenPrint$thenWorksFine() {
    buildAttemptAction().accept(TestUtils.createPrintingActionScanner(Writer.Std.OUT));
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
        e -> simple(
            "print stacktrace",
            () -> e.get().printStackTrace(System.out))
    ).ensure(simple(
        "print bye",
        () -> System.out.println("Bye 'attempt'"))
    );
  }
}
