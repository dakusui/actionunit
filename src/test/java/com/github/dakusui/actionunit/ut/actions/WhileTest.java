package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.ActionSupport;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.ut.utils.TestUtils;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import static com.github.dakusui.pcond.TestAssertions.assertThat;
import static com.github.dakusui.pcond.forms.Printables.predicate;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.function.Predicate.isEqual;

public class WhileTest extends TestUtils.TestBase {
  @Test
  public void givenWhileAction_whenPerform_thenRepeatsWhileConditionIsTrue() {
    List<String> out = new LinkedList<>();
    Action whileAction = ActionSupport.repeatWhile(iIsLessThan10()).action(printAndIncrementI(out)).build();
    TestUtils.createReportingActionPerformer().performAndReport(whileAction, Writer.Std.OUT);
    assertThat(out, isEqual(asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")));
  }

  @Test
  public void givenWhileActionWithConditionNeverMet_whenPerform_thenNothingHappens() {
    List<String> out = new LinkedList<>();
    Action whileAction = ActionSupport.repeatWhile(iIsGreaterThan0()).action(printAndIncrementI(out)).build();
    TestUtils.createReportingActionPerformer().performAndReport(whileAction, Writer.Std.OUT);
    assertThat(out, isEqual(emptyList()));
  }


  private static Action printAndIncrementI(List<String> out) {
    return ActionSupport.simple("print:i++", c -> {
      int i = c.defined("i") ? c.valueOf("i") : 0;
      System.out.println(i);
      out.add(Integer.toString(i));
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

  private static Predicate<Context> iIsGreaterThan0() {
    return predicate("i is greater than 0", (Context context) -> {
      int i = context.defined("i") ? context.valueOf("i") : 0;
      return i > 0;
    });
  }
}
