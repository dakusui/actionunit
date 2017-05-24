package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.actions.Attempt;
import com.github.dakusui.actionunit.visitors.ActionPrinter;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import org.junit.Test;

import static com.github.dakusui.actionunit.helpers.Actions.sequential;
import static com.github.dakusui.actionunit.helpers.Actions.simple;
import static com.github.dakusui.actionunit.helpers.Builders.attempt;

public class AttemptExample {
  @Test(expected = IllegalArgumentException.class)
  public void givenAttemptAction$whenPerform$thenWorksFine() {
    buildAttemptAction().accept(new ActionRunner.Impl());
  }

  @Test
  public void givenAttemptAction$whenPrint$thenWorksFine() {
    buildAttemptAction().accept(new ActionPrinter(ActionPrinter.Writer.Std.OUT));
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
            () -> {
              e.get().printStackTrace(System.out);
            })
    ).ensure(simple(
        "print bye",
        () -> {
          System.out.println("Bye 'attempt'");
        })
    );
  }
}
