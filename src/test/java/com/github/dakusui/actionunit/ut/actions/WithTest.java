package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.ActionSupport;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.ut.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.ActionPrinter;
import com.github.dakusui.printables.PrintableFunctionals;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.github.dakusui.actionunit.core.ActionSupport.*;
import static com.github.dakusui.pcond.fluent.Fluents.assertThat;
import static com.github.dakusui.pcond.fluent.Fluents.value;
import static com.github.dakusui.pcond.forms.Functions.identity;
import static com.github.dakusui.pcond.forms.Predicates.lessThan;
import static com.github.dakusui.pcond.forms.Predicates.lt;
import static com.github.dakusui.printables.PrintableFunctionals.printableConsumer;
import static com.github.dakusui.printables.PrintableFunctionals.printableFunction;

public class WithTest {
  @Test
  public void givenWithAction_whenPerform() {
    List<String> out = new LinkedList<>();

    Action withAction = with(constant(0)).action(leaf(println(out))).build();
    TestUtils.createReportingActionPerformer().performAndReport(withAction, Writer.Std.OUT);
    assertThat(value(out).elementAt(0).then().asString().isNotNull());
  }

  @Test
  public void givenWithAction_whenPerform2() {
    List<String> out = new LinkedList<>();
    Action withAction = with(constant(10))
        .action(b -> simple("printVariable", b.consumer((Integer i) -> println(out).accept(i + 1))))
        .build();
    TestUtils.createReportingActionPerformer().performAndReport(withAction, Writer.Std.OUT);
    assertThat(value(out).elementAt(0).then().asString().isEqualTo("11"));
  }

  @Test
  public void givenWithAction_whenPerform3() {
    List<String> out = new LinkedList<>();
    Action withAction = with(constant(10))
        .action(b -> when(b.predicate(lessThan(100)))
            .perform(leaf(b.consumer(println(out))))
            .otherwise(ActionSupport.nop()))
        .build();
    TestUtils.createReportingActionPerformer().performAndReport(withAction, Writer.Std.OUT);

    assertThat(value(out).elementAt(0).then().asString().isEqualTo("10"));
  }

  @Test
  public void printActionTree() {
    Action withAction = with(constant(9))
        .action(b -> when(b.predicate(lessThan(10)))
            .perform(leaf(b.consumer(i -> System.out.println("<" + i + ">"))))
            .otherwise(ActionSupport.nop()))
        .build();
    withAction.accept(new ActionPrinter(Writer.Std.OUT));
  }

  @Test
  public void printActionTree_2() {
    Action withAction = with(constant(9))
        .action(b -> when(b.predicate(lessThan(10)))
            .perform(leaf(b.consumer(i -> System.out.println("<" + i + ">"))))
            .otherwise(ActionSupport.nop()))
        .build(printVariable());
    withAction.accept(new ActionPrinter(Writer.Std.OUT));
  }

  @Test
  public void printActionTree_3() {
    Action withAction = with(constant(0))
        .action(b -> when(b.predicate(lessThan(10)))
            .perform(leaf(b.consumer(i -> System.out.println("<" + i + ">"))))
            .otherwise(ActionSupport.nop()))
        .build(printVariable());
    withAction.accept(new ActionPrinter(Writer.Std.OUT));
  }

  @Test
  public void printActionTree_4() {
    List<String> out = new LinkedList<>();
    Action withAction = with(constant(0))
        .action(
            b -> b.nest(identity())
                .action(named("HELLO", nop()))
                .build(println(out)))
        .build(printVariable());
    withAction.accept(new ActionPrinter(Writer.Std.OUT));
  }

  @Test
  public void printActionTree_5() {
    Action withAction = with(constant(0))
        .action(b -> with(constant(0))
            .action(nop())
            .$())
        .build(printVariable());
    withAction.accept(new ActionPrinter(Writer.Std.OUT));

    TestUtils.createReportingActionPerformer().performAndReport(withAction, Writer.Std.OUT);
  }

  @Test
  public void printActionTree_6() {
    Action withAction = with(constant(1))
        .action(b -> repeatWhile(b.predicate(lt(10)))
            .perform(sequential(
                b.createAction(printVariable()),
                b.updateVariableWith(increment())))
            .build())
        .build(printVariable());
    withAction.accept(new ActionPrinter(Writer.Std.OUT));
    TestUtils.createReportingActionPerformer().performAndReport(withAction, Writer.Std.OUT);
  }

  private static <T, R> Function<T, R> constant(R value) {
    return printableFunction((T in) -> value).describe(Objects.toString(value));
  }

  private static Consumer<Integer> printVariable() {
    return printableConsumer((Integer i) -> System.out.println(i)).describe("printVariable");
  }

  private static Function<Integer, Integer> increment() {
    return PrintableFunctionals.<Integer, Integer>printableFunction(i -> i + 1).describe("increment");
  }

  private static <T> Consumer<T> println(List<String> out) {
    return new Consumer<T>() {
      @Override
      public void accept(T value) {
        System.out.println(value);
        out.add(Objects.toString(value));
      }

      @Override
      public String toString() {
        return "System.out::println";
      }
    };
  }
}
