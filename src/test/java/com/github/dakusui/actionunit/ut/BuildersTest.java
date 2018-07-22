package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.actions.Leaf;
import com.github.dakusui.actionunit.actions.ValueHolder;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.compat.ValueHandlerActionFactory;
import com.github.dakusui.actionunit.generators.ActionGenerator;
import com.github.dakusui.actionunit.examples.UtContext;
import com.github.dakusui.actionunit.helpers.Checks;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.PrintingActionScanner;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static java.util.Arrays.asList;

@RunWith(Enclosed.class)
public class BuildersTest implements UtContext {
  public static <T> ValueHandlerActionFactory<T> createValueHandlerActionFactory(String description, Consumer<T> handlerBody) {
    Objects.requireNonNull(handlerBody);
    return new ValueHandlerActionFactory<T>() {
      private Bean bean = new Bean();

      @Override
      public Bean bean() {
        return this.bean;
      }

      @Override
      public Action create(Context context, ValueHolder<T> valueHolder) {
        return Leaf.create(this.generateId(), description, () -> handlerBody.accept(valueHolder.get()));
      }
    };
  }

  public static <E> ActionGenerator<E> actionGeneratorFromValueActionHandler(ValueHandlerActionFactory<E> operation) {
    return ActionGenerator.<E>of(valueHolder -> context -> operation.create(context, valueHolder));
  }

  public static class ForEachTest implements UtContext {
    @Test
    public void givenA_B_and_C$whenRunForEachSequentially$thenWorksFine() {
      Action action = forEachOf("A", "B", "C")
          .sequentially()
          .perform(actionGeneratorFromValueActionHandler(createValueHandlerActionFactory("print item to stdout", System.out::println)));
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
          .perform(actionGeneratorFromValueActionHandler(createValueHandlerActionFactory("print item to stdout", System.out::println)));
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
          .perform(actionGeneratorFromValueActionHandler(createValueHandlerActionFactory("print item to stdout", System.out::println)));
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
          .perform(actionGeneratorFromValueActionHandler(createValueHandlerActionFactory("print item to stdout", System.out::println)));
      try {
        action.accept(TestUtils.createActionPerformer());
      } finally {
        action.accept(TestUtils.createPrintingActionScanner());
      }
    }
  }

  public static class AttemptTest implements UtContext {
    @Test(expected = IllegalStateException.class)
    public void given$when$then() {
      Action action = this.attempt(
          this.simple("throw IllegalStateException", () -> {
            throw new IllegalStateException();
          })
      ).recover(
          RuntimeException.class,
          e -> ($) -> $.concurrent(
              $.simple("print capture", () -> {
              }),
              $.simple("print stacktrace", () -> {
                e.get().printStackTrace(System.out);
                throw Checks.propagate(e.get());
              }),
              $.simple("print recovery", () -> System.out.println("Recovered."))
          )
      ).ensure(
          v -> ($) -> $.simple("Say 'bye'", () -> System.out.println("Bye"))
      );
      try {
        action.accept(TestUtils.createActionPerformer());
      } finally {
        action.accept(PrintingActionScanner.Factory.DEFAULT_INSTANCE.create(Writer.Std.OUT));
      }
    }
  }
}
