package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.ut.utils.TestUtils;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static com.github.dakusui.actionunit.core.ActionSupport.simple;
import static com.github.dakusui.actionunit.core.ActionSupport.with;

public class WithTest {
  @Test
  public void givenWithAction_whenPerform() {
    List<String> out = new LinkedList<>();
    Action withAction = with(c -> 0).perform(println(out)).build();
    TestUtils.createReportingActionPerformer().performAndReport(withAction, Writer.Std.OUT);
  }

  @Test
  public void givenWithAction_whenPerform2() {
    Action withAction = with(c -> 10)
        .action(b -> simple("printVariable", b.consumer(i -> System.out.println(i + 1))))
        .build();
    TestUtils.createReportingActionPerformer().performAndReport(withAction, Writer.Std.OUT);
  }

  private static Consumer<Integer> println(List<String> out) {
    return new Consumer<Integer>() {
      @Override
      public void accept(Integer value) {
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
