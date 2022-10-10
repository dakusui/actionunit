package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.ActionSupport;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.ut.utils.TestUtils;
import org.junit.Test;

import java.util.function.Predicate;

import static com.github.dakusui.pcond.forms.Printables.predicate;

public class WhileTest {
  @Test
  public void givenWhileAction_whenPerform_thenRepeatsWhileConditionIsTrue() {
    Action whileAction = ActionSupport.repeatWhile(iIsLessThan10()).perform(printAndIncrementI()).build();
    TestUtils.createReportingActionPerformer().performAndReport(whileAction, Writer.Std.OUT);
  }

  private static Action printAndIncrementI() {
    return ActionSupport.simple("print:i++", c -> {
      int i = c.defined("i") ? c.valueOf("i") : 0;
      System.out.println(i);
      i++;
      c.assignTo("i", i);
    });
  }

  private static Predicate<Context> iIsLessThan10() {
    return predicate("i is less than 10", (Context context) -> {
      int i = context.defined("i") ? context.valueOf("i") : 0;
      return i < 10;
    });
  }
}
