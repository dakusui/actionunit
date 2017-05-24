package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.actions.TestAction;
import com.github.dakusui.actionunit.exceptions.ActionAssertionError;
import com.github.dakusui.actionunit.visitors.ActionPrinter;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import org.junit.Test;

public class TestActionExample {
  @Test(expected = ActionAssertionError.class)
  public void givenIncorrectTest$whenRunTest$thenExceptionThrown() {
    Action testAction = new TestAction.Builder<String, Integer>()
        .given("'Hello world'", () -> {
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
        })
        .build();
    try {
      testAction.accept(new ActionRunner.Impl());
    } finally {
      testAction.accept(new ActionPrinter(ActionPrinter.Writer.Std.OUT));
    }
  }


  @Test
  public void givenCorrectTest$whenRunTest$thenPass() {
    new TestAction.Builder<String, Integer>()
        .given("Hello world", () -> "Hello world")
        .when("length", String::length)
        .then(">5", i -> i > 5)
        .build()
        .accept(new ActionRunner.Impl());
  }
}
