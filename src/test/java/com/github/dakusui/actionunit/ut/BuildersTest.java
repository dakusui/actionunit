package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.ActionSupport;
import com.github.dakusui.actionunit.helpers.Checks;
import com.github.dakusui.actionunit.helpers.Utils;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.PrintingActionScanner;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.List;

import static com.github.dakusui.actionunit.core.ActionSupport.concurrent;
import static com.github.dakusui.actionunit.core.ActionSupport.simple;
import static java.util.Arrays.asList;

@RunWith(Enclosed.class)
public class BuildersTest {
  public static class ForEachTest {
    @Test
    public void givenA_B_and_C$whenRunForEachSequentially$thenWorksFine() {
      Action action = ActionSupport.forEachOf("A", "B", "C")
          .sequentially()
          .perform(Utils.handlerFactory("print item to stdout", System.out::println));
      try {
        action.accept(TestUtils.createActionPerformer());
      } finally {
        action.accept(TestUtils.createPrintingActionScanner());
      }
    }

    @Test
    public void givenA_B_and_C$whenPrintForEachSequentially$thenWorksFine() {
      Action action = ActionSupport.forEachOf("A", "B", "C")
          .sequentially()
          .perform(Utils.handlerFactory("print item to stdout", System.out::println));
      try {
        action.accept(TestUtils.createActionPerformer());
      } finally {
        action.accept(TestUtils.createPrintingActionScanner());
      }
    }

    @Test
    public void givenA_B_and_C$whenRunForEachConcurrently$thenWorksFine() {
      Action action = ActionSupport.forEachOf("A", "B", "C")
          .concurrently()
          .perform(Utils.handlerFactory("print item to stdout", System.out::println));
      try {
        action.accept(TestUtils.createActionPerformer());
      } finally {
        action.accept(TestUtils.createPrintingActionScanner());
      }
    }

    @Test
    public void givenA_B_and_CAsList$whenRunForEachConcurrently$thenWorksFine() {
      List<Object> data = asList("A", "B", "C");
      Action action = ActionSupport.forEachOf(data)
          .concurrently()
          .perform(Utils.handlerFactory("print item to stdout", System.out::println));
      try {
        action.accept(TestUtils.createActionPerformer());
      } finally {
        action.accept(TestUtils.createPrintingActionScanner());
      }
    }
  }

  public static class AttemptTest {
    @Test(expected = IllegalStateException.class)
    public void given$when$then() {
      Action action = ActionSupport.attempt(
          simple("throw IllegalStateException", () -> {
            throw new IllegalStateException();
          })
      ).recover(
          RuntimeException.class,
          ($, e) -> concurrent(
              simple("print capture", () -> {
              }),
              simple("print stacktrace", () -> {
                e.get().printStackTrace(System.out);
                throw Checks.propagate(e.get());
              }),
              simple("print recovery", () -> {
                System.out.println("Recovered.");
              })
          )
      ).ensure(
          ($, e) -> $.simple("Say 'bye'", () -> System.out.println("Bye"))
      );
      try {
        action.accept(TestUtils.createActionPerformer());
      } finally {
        action.accept(PrintingActionScanner.Factory.DEFAULT_INSTANCE.create(Writer.Std.OUT));
      }
    }
  }
}
