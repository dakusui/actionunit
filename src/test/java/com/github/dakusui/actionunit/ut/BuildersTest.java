package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.actions.ValueHolder;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.ValueHandlerActionFactory;
import com.github.dakusui.actionunit.helpers.Checks;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.PrintingActionScanner;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static com.github.dakusui.actionunit.core.ActionFactory.ID_GENERATOR_MANAGER;
import static java.util.Arrays.asList;

@RunWith(Enclosed.class)
public class BuildersTest implements Context {
  public AtomicInteger idGenerator() {
    return ID_GENERATOR_MANAGER.idGenerator(this);
  }
  public static class ForEachTest implements Context {
    public AtomicInteger idGenerator() {
      return ID_GENERATOR_MANAGER.idGenerator(this);
    }
    @Test
    public void givenA_B_and_C$whenRunForEachSequentially$thenWorksFine() {
      Action action = forEachOf("A", "B", "C")
          .sequentially()
          .perform(ValueHandlerActionFactory.create("print item to stdout", System.out::println));
      try {
        action.accept(TestUtils.createActionPerformer());
      } finally {
        action.accept(TestUtils.createPrintingActionScanner());
      }
    }

    @Test
    public void givenA_B_and_C$whenPrintForEachSequentially$thenWorksFine() {
      Action action = forEachOf("A", "B", "C")
          .sequentially()
          .perform(ValueHandlerActionFactory.create("print item to stdout", System.out::println));
      try {
        action.accept(TestUtils.createActionPerformer());
      } finally {
        action.accept(TestUtils.createPrintingActionScanner());
      }
    }

    @Test
    public void givenA_B_and_C$whenRunForEachConcurrently$thenWorksFine() {
      Action action = forEachOf("A", "B", "C")
          .concurrently()
          .perform(ValueHandlerActionFactory.create("print item to stdout", System.out::println));
      try {
        action.accept(TestUtils.createActionPerformer());
      } finally {
        action.accept(TestUtils.createPrintingActionScanner());
      }
    }

    @Test
    public void givenA_B_and_CAsList$whenRunForEachConcurrently$thenWorksFine() {
      List<Object> data = asList("A", "B", "C");
      Action action = forEachOf(data)
          .concurrently()
          .perform(ValueHandlerActionFactory.create("print item to stdout", System.out::println));
      try {
        action.accept(TestUtils.createActionPerformer());
      } finally {
        action.accept(TestUtils.createPrintingActionScanner());
      }
    }
  }

  public static class AttemptTest implements Context {
    public AtomicInteger idGenerator() {
      return ID_GENERATOR_MANAGER.idGenerator(this);
    }

    @Test(expected = IllegalStateException.class)
    public void given$when$then() {
      Action action = this.attempt(
          this.simple("throw IllegalStateException", () -> {
            throw new IllegalStateException();
          })
      ).recover(
          RuntimeException.class,
          ($, e) -> $.concurrent(
              $.simple("print capture", () -> {
              }),
              $.simple("print stacktrace", () -> {
                ((Throwable) e.get()).printStackTrace(System.out);
                throw Checks.propagate((Throwable) e.get());
              }),
              $.simple("print recovery", () -> System.out.println("Recovered."))
          )
      ).ensure(
          ($) -> $.simple("Say 'bye'", () -> System.out.println("Bye"))
      );
      try {
        action.accept(TestUtils.createActionPerformer());
      } finally {
        action.accept(PrintingActionScanner.Factory.DEFAULT_INSTANCE.create(Writer.Std.OUT));
      }
    }
  }
}
