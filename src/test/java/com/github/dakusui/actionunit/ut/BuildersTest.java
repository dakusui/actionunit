package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.helpers.Checks;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.visitors.ActionPerformer;
import com.github.dakusui.actionunit.visitors.ActionPrinter;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.List;

import static com.github.dakusui.actionunit.helpers.Actions.concurrent;
import static com.github.dakusui.actionunit.helpers.Actions.simple;
import static com.github.dakusui.actionunit.helpers.Builders.*;
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
        action.accept(new ActionPerformer.Impl());
      } finally {
        action.accept(new ActionPrinter.Impl(Writer.Std.OUT));
      }
    }

    @Test
    public void givenA_B_and_C$whenPrintForEachSequentially$thenWorksFine() {
      Action action = forEachOf("A", "B", "C")
          .sequentially()
          .perform(handlerFactory("print item to stdout", System.out::println));
      try {
        action.accept(new ActionPerformer.Impl());
      } finally {
        action.accept(new ActionPrinter.Impl(Writer.Std.OUT));
      }
    }

    @Test
    public void givenA_B_and_C$whenRunForEachConcurrently$thenWorksFine() {
      Action action = forEachOf("A", "B", "C")
          .concurrently()
          .perform(handlerFactory("print item to stdout", System.out::println));
      try {
        action.accept(new ActionPerformer.Impl());
      } finally {
        action.accept(new ActionPrinter.Impl(Writer.Std.OUT));
      }
    }

    @Test
    public void givenA_B_and_CAsList$whenRunForEachConcurrently$thenWorksFine() {
      List<Object> data = asList("A", "B", "C");
      Action action = forEachOf(data)
          .concurrently()
          .perform(handlerFactory("print item to stdout", System.out::println));
      try {
        action.accept(new ActionPerformer.Impl());
      } finally {
        action.accept(new ActionPrinter.Impl(Writer.Std.OUT));
      }
    }
  }

  public static class AttemptTest {
    @Test(expected = IllegalStateException.class)
    public void given$when$then() {
      Action action = attempt(
          simple("throw IllegalStateException", () -> {
            throw new IllegalStateException();
          })
      ).recover(
          RuntimeException.class,
          e -> concurrent(
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
          simple("Say 'bye'", () -> System.out.println("Bye"))
      );
      try {
        action.accept(new ActionPerformer.Impl());
      } finally {
        action.accept(new ActionPrinter.Impl(Writer.Std.OUT));
      }
    }
  }
}
