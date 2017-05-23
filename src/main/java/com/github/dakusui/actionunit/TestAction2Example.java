package com.github.dakusui.actionunit;

import com.github.dakusui.actionunit.actions.TestAction2;
import com.github.dakusui.actionunit.exceptions.ActionAssertionError;
import com.github.dakusui.actionunit.visitors.ActionPrinter;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import org.junit.Test;

public class TestAction2Example {
  @Test(expected = ActionAssertionError.class)
  public void givenIncorrectTest$whenRunTest$thenExceptionThrown() {
    Action testAction = new TestAction2.Builder<String, Integer>()
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
      testAction.accept(new ActionPrinter<>(ActionPrinter.Writer.Std.OUT));
    }
  }


  @Test
  public void givenCorrectTest$whenRunTest$thenPass() {
    new TestAction2.Builder<String, Integer>()
        .given("Hello world", () -> "Hello world")
        .when("length", String::length)
        .then(">5", i -> i > 5)
        .build()
        .accept(new ActionRunner.Impl());
  }
}
