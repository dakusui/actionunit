package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.ActionUnit;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.ActionFactory;
import com.github.dakusui.actionunit.visitors.reporting.Report;
import com.github.dakusui.actionunit.visitors.reporting.ReportingActionPerformer;
import org.junit.Test;
import org.junit.runner.RunWith;

import static java.util.Arrays.asList;

@RunWith(ActionUnit.class)
public class NewStyleExample implements ActionFactory {
  @ActionUnit.PerformWith(Test.class)
  public Action createAction() {
    return this.get();
  }

  @Test
  public void run(Action action) {
    new ReportingActionPerformer.Builder(
        action
    ).with(
        Report.Record.Formatter.DEFAULT_INSTANCE
    ).build(
    ).performAndReport();
  }

  @Override
  public Action create(ActionFactory self) {
    return self.forEachOf(
        asList("a", "b", "c")
    ).sequentially(
    ).perform(
        (d, i) ->
            d.sequential(
                d.simple(
                    "print(i)",
                    () -> System.out.println(i.get())
                ),
                d.forEachOf(asList(
                    "1", "2", "3"
                )).perform(
                    (e, j) -> e.simple(
                        "print(j)",
                        () -> System.out.println(j.get())
                    )))
    );
  }
}
