package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.actions.Attempt;
import com.github.dakusui.actionunit.actions.HandlerFactory;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.ActionFactory;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.utils.TestUtils;
import org.junit.Test;

import java.util.function.Supplier;

import static com.github.dakusui.actionunit.core.ActionSupport.*;

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
        new HandlerFactory.Base<Throwable>() {
          @Override
          public Action create(ActionFactory $, Supplier<Throwable> e) {
            return simple(
                "print stacktrace",
                () -> {
                });
          }
        }
    ).ensure(($, $_) -> $.simple(
        "print bye",
        () -> System.out.println("Bye 'attempt'"))
    );
  }
}
