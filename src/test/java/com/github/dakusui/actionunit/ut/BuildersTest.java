package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.compat.CompatActions;
import com.github.dakusui.actionunit.visitors.ActionPrinter;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.List;

import static com.github.dakusui.actionunit.Builders.*;
import static java.util.Arrays.asList;

@RunWith(Enclosed.class)
public class BuildersTest {
  public static class ForEachTest {
    @Test
    public void givenA_B_and_C$whenRunForEachSequentially$thenWorksFine() {
      Action action = forEachOf("A", "B", "C")
          .sequentially()
          .perform(handlerFactory("print item to stdout", System.out::println));
      try {
        action.accept(new ActionRunner.Impl());
      } finally {
        action.accept(new ActionPrinter(ActionPrinter.Writer.Std.OUT));
      }
    }

    @Test
    public void givenA_B_and_C$whenPrintForEachSequentially$thenWorksFine() {
      Action action = forEachOf("A", "B", "C")
          .sequentially()
          .perform(handlerFactory("print item to stdout", System.out::println));
      try {
        action.accept(new ActionRunner.Impl());
      } finally {
        action.accept(new ActionPrinter(ActionPrinter.Writer.Std.OUT));
      }
    }

    @Test
    public void givenA_B_and_C$whenRunForEachConcurrently$thenWorksFine() {
      Action action = forEachOf("A", "B", "C")
          .concurrently()
          .perform(handlerFactory("print item to stdout", System.out::println));
      try {
        action.accept(new ActionRunner.Impl());
      } finally {
        action.accept(new ActionPrinter(ActionPrinter.Writer.Std.OUT));
      }
    }

    @Test
    public void givenA_B_and_CAsList$whenRunForEachConcurrently$thenWorksFine() {
      List<Object> data = asList("A", "B", "C");
      Action action = forEachOf(data)
          .concurrently()
          .perform(handlerFactory("print item to stdout", System.out::println));
      try {
        action.accept(new ActionRunner.Impl());
      } finally {
        action.accept(new ActionPrinter(ActionPrinter.Writer.Std.OUT));
      }
    }
  }

  public static class AttemptTest {
    @Test
    public void given$when$then() {
      Action action = attempt(CompatActions.createLeafAction(() -> {
            throw new IllegalStateException();
          })
      ).recover(
          RuntimeException.class,
          handlerFactory("print messages and stack trace.", e -> {
            System.out.println("Following is the captured exception.");
            e.printStackTrace();
            System.out.println("Recovered.");
          })
      ).ensure(
          CompatActions.createLeafAction(() -> System.out.println("Bye"))
      );
      try {
        action.accept(new ActionRunner.Impl());
      } finally {
        action.accept(new ActionPrinter(ActionPrinter.Writer.Std.OUT));
      }
    }
  }
}
