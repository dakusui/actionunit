package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.compat.ActionUnit;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.n.io.Writer;
import com.github.dakusui.actionunit.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.reporting.Report;
import com.github.dakusui.actionunit.visitors.reporting.ReportingActionPerformer;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicInteger;

import static com.github.dakusui.actionunit.utils.Utils.toSupplier;

@RunWith(ActionUnit.class)
public class WhileExample extends TestUtils.ContextTestBase implements Context {
  @ActionUnit.PerformWith(Test.class)
  public Action composeWhileLoop() {
    /*
     * This method is an ActionUnit style equivalence of following code fragment.
     *     int i = 0;
     *     while (i++ < 10)
     *       System.err.println(i);
     */
    AtomicInteger v = new AtomicInteger(0);
    return whilst(
        toSupplier(v),
        i -> i.getAndIncrement() < 10
    ).perform(
        w -> self -> self.simple("print v", () -> System.out.println(v.get()))
    );
  }

  @Test
  public void runAction2(Action action) {
    new ReportingActionPerformer.Builder(action)
        .with(Report.Record.Formatter.DEFAULT_INSTANCE)
        .to(Writer.Std.OUT)
        .build()
        .performAndReport();
  }
}
