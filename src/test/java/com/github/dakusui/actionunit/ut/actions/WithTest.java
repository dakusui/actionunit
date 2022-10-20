package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.ActionSupport;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.ut.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.ActionPrinter;
import com.github.dakusui.pcond.forms.Predicates;
import com.github.dakusui.pcond.forms.Printables;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static com.github.dakusui.actionunit.core.ActionSupport.*;
import static com.github.dakusui.pcond.fluent.Fluents.assertThat;
import static com.github.dakusui.pcond.fluent.Fluents.value;

public class WithTest {
  @Test
  public void givenWithAction_whenPerform() {
    List<String> out = new LinkedList<>();
    Action withAction = with(c -> 0).perform(println(out)).build();
    TestUtils.createReportingActionPerformer().performAndReport(withAction, Writer.Std.OUT);
    assertThat(value(out).elementAt(0).then().asString().isEqualTo("0"));
  }

  @Test
  public void givenWithAction_whenPerform2() {
    List<String> out = new LinkedList<>();
    Action withAction = with(c -> 10)
        .action(b -> simple("printVariable", b.consumer((Integer i) -> println(out).accept(i + 1))))
        .build();
    TestUtils.createReportingActionPerformer().performAndReport(withAction, Writer.Std.OUT);
    assertThat(value(out).elementAt(0).then().asString().isEqualTo("11"));
  }

  @Test
  public void givenWithAction_whenPerform3() {
    List<String> out = new LinkedList<>();
    Action withAction = with(c -> 10)
        .action(b -> when(b.predicate(i -> i < 100))
            .perform(leaf(b.consumer(println(out))))
            .otherwise(ActionSupport.nop()))
        .build();
    TestUtils.createReportingActionPerformer().performAndReport(withAction, Writer.Std.OUT);
    assertThat(value(out).elementAt(0).then().asString().isEqualTo("10"));
  }

  @Test
  public void printActionTree() {
    Action withAction = with(Printables.function("=9", c -> 9))
        .action(b -> when(b.predicate(Predicates.lessThan(10)))
            .perform(leaf(b.consumer(i -> System.out.println("<" + i + ">"))))
            .otherwise(ActionSupport.nop()))
        .build();
    withAction.accept(new ActionPrinter(Writer.Std.OUT));
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
