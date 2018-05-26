package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.visitors.reporting.ReportingActionPerformer;
import org.junit.Test;

import static java.util.Arrays.asList;

public class VariableFeatureExample {
  @Test
  public void example() {
    Context top = new Context.Impl();

    run(
        top.sequential(
            top.simple("init", () -> top.set("i", 0)),
            top.forEachOf(asList("a", "b", "c", "d", "e", "f"))
                .perform(
                    ($, data) ->
                        $.sequential(
                            $.simple(
                                "print i",
                                () -> System.out.printf("%d %s%n", top.<Integer>get("i"), data.get())
                            ),
                            $.simple(
                                "i++",
                                () -> top.set("i", top.<Integer>get("i") + 1)
                            ))))
    );
  }

  private void run(Action action) {
    ReportingActionPerformer performer = ReportingActionPerformer.create(action);
    performer.report();
    performer.performAndReport();
  }
}
