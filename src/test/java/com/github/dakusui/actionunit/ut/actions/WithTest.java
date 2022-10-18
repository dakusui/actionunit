package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.ActionSupport;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.ut.utils.TestUtils;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class WithTest {
  @Test
  public void givenWithAction_whenPerform() {
    List<String> out = new LinkedList<>();
    Action withAction = ActionSupport.with(c -> 0).thenConsumeBy(println()).build();
    TestUtils.createReportingActionPerformer().performAndReport(withAction, Writer.Std.OUT);
  }

  private static Consumer<Integer> println() {
    return new Consumer<Integer>() {
      @Override
      public void accept(Integer integer) {
        System.out.println(integer);
      }

      @Override
      public String toString() {
        return "System.out::println";
      }
    };
  }
}
