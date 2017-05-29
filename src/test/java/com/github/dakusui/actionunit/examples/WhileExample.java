package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.ActionUnit;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.helpers.Actions2;
import com.github.dakusui.actionunit.helpers.Builders2;
import com.github.dakusui.actionunit.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.Report;
import com.github.dakusui.actionunit.visitors.ReportingActionRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicInteger;

import static com.github.dakusui.actionunit.helpers.Utils.toSupplier;

@RunWith(ActionUnit.class)
public class WhileExample extends TestUtils.TestBase implements Actions2, Builders2 {
  @ActionUnit.PerformWith(Test.class)
  public Action composeWhileLoop() {
    /**
     * This method is an ActionUnit style equivalence of following code fragment.
     *     int i = 0;
     *     while (i++ < 10)
     *       System.err.println(i);
     */
    return whilst(
        toSupplier(new AtomicInteger(0)),
        i -> i.getAndIncrement() < 10
    ).perform(
        i -> simple("print i", () -> System.out.println("i:" + i.get()))
    ).$();
  }

  @Test
  public void runAction2(Action action) {
    new ReportingActionRunner.Builder(action)
        .with(Report.Record.Formatter.DEFAULT_INSTANCE)
        .to(ReportingActionRunner.Writer.Std.OUT)
        .setThreadPoolSize(2)
        .build()
        .perform();
  }
}
