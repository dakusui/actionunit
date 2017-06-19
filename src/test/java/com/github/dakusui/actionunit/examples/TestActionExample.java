package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.exceptions.ActionAssertionError;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.PrintingActionScanner;
import org.junit.Test;

public class TestActionExample implements Context {
  @Test(expected = ActionAssertionError.class)
  public void givenIncorrectTest$whenRunTest$thenExceptionThrown() {
    Action testAction = this.<String, Integer>given("'Hello world'", () -> {
      System.err.println(":Hello world");
      return "Hello world";
    }).when("length", s -> {
      System.err.println(":length");
      return s.length();
    }).then(">20", i -> {
      System.err.println(":>20");
      return i > 20;
    });
    try {
      testAction.accept(TestUtils.createActionPerformer());
    } finally {
      testAction.accept(PrintingActionScanner.Factory.DEFAULT_INSTANCE.create(Writer.Std.OUT));
    }
  }


  @Test
  public void givenCorrectTest$whenRunTest$thenPass() {

    this.<String, Integer>given(
        "Hello world", () -> "Hello world"
    ).when(
        "length", String::length
    ).then(
        ">5", i -> i > 5
    ).accept(
        TestUtils.createActionPerformer()
    );
  }
}
