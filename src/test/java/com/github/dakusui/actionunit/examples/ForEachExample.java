package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.ActionUnit;
import com.github.dakusui.actionunit.ActionUnit.PerformWith;
import com.github.dakusui.actionunit.compat.visitors.CompatActionRunnerWithResult;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.helpers.Actions2;
import com.github.dakusui.actionunit.helpers.Builders2;
import com.github.dakusui.actionunit.visitors.ActionPrinter;
import org.junit.Test;
import org.junit.runner.RunWith;

import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@RunWith(ActionUnit.class)
public class ForEachExample implements Actions2, Builders2 {
  @PerformWith(Test.class)
  public Action composeSingleLoop() {
    return forEachOf(
        asList("A", "B", "C")
    ).sequentially(
    ).perform(
        value -> sequential(
            simple("print the given value(1st time)", () -> System.out.println(value.get())),
            simple("print the given value(2nd time)", () -> System.out.println(value.get()))
        )
    );
  }

  @PerformWith(Test.class)
  public Action composeNestedLoop() {
    return forEachOf(
        "A", "B", "C"
    ).sequentially(
    ).perform(
        i -> sequential(
            simple("print the given value(1st time)", () -> System.out.println("BEGIN:" + i.get())),
            forEachOf(
                "a", "b", "c"
            ).sequentially(
            ).perform(
                j -> sequential(
                    sleep(1, MILLISECONDS),
                    simple("print i and j", () -> System.out.printf("  i=%s, j=%s%n", i.get(), j.get()))
                )
            ),
            simple("print the given value(2nd time)", () -> System.out.println("END:" + i.get()))
        )
    );
  }

  @Test
  public void runAction(Action action) {
    CompatActionRunnerWithResult runner = new CompatActionRunnerWithResult();
    try {
      action.accept(runner);
    } finally {
      action.accept(ActionPrinter.Factory.REPORTER.stdout());
    }
  }
}
