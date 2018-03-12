package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.ActionUnit;
import com.github.dakusui.actionunit.actions.ValueHolder;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.visitors.reporting.ReportingActionPerformer;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ActionUnit.class)
@FixMethodOrder
public class HelloActionUnit implements Context {
  @ActionUnit.PerformWith(Test.class)
  public Action helloActionUnit() {
    return forEachOf(
        "Hello", "world", "!"
    ).concurrently(
    ).perform(
        (Context $, ValueHolder<String> i) -> sequential(
            simple(
                "print {i}",
                () -> System.out.println("<" + i.get() + ">")
            )
        )
    );
  }

  @Test
  public void printOnly(Action action) {
    new ReportingActionPerformer.Builder(action)
        .to(Writer.Std.ERR)
        .build()
        .report();
  }

  @Test
  public void performOnly(Action action) {
    new ReportingActionPerformer.Builder(action)
        .to(Writer.Std.ERR)
        .build()
        .perform();
  }

  @Test
  public void runAndReport(Action action) {
    new ReportingActionPerformer.Builder(action)
        .to(Writer.Std.ERR)
        .build()
        .performAndReport();
  }
}