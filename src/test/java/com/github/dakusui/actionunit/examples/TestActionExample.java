package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.exceptions.ActionAssertionError;
import com.github.dakusui.actionunit.helpers.Builders;
import com.github.dakusui.actionunit.visitors.ActionPerformer;
import com.github.dakusui.actionunit.visitors.ActionPrinter;
import com.github.dakusui.actionunit.visitors.ReportingActionRunner;
import org.junit.Test;

public class TestActionExample {
  @Test(expected = ActionAssertionError.class)
  public void givenIncorrectTest$whenRunTest$thenExceptionThrown() {
    Action testAction = Builders
        .<String, Integer>given("'Hello world'", () -> {
          System.err.println(":Hello world");
          return "Hello world";
        })
        .when("length", s -> {
          System.err.println(":length");
          return s.length();
        })
        .then(">20", i -> {
          System.err.println(":>20");
          return i > 20;
        });
    try {
      testAction.accept(new ActionPerformer.Impl());
    } finally {
      testAction.accept(new ActionPrinter.Impl(ReportingActionRunner.Writer.Std.OUT));
    }
  }


  @Test
  public void givenCorrectTest$whenRunTest$thenPass() {
    Builders
        .<String, Integer>given("Hello world", () -> "Hello world")
        .when("length", String::length)
        .then(">5", i -> i > 5)
        .accept(new ActionPerformer.Impl());
  }
}
