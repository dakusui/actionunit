package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.ActionUnit;
import com.github.dakusui.actionunit.ActionUnit.PerformWith;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.github.dakusui.actionunit.Actions.*;
import static com.github.dakusui.actionunit.Builders.forEachOf;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@RunWith(ActionUnit.class)
public class ForEach2Example {
  @PerformWith(Test.class)
  public Action composeSingleLoop() {
    return forEachOf(
        "A", "B", "C"
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
            simple("print the given value(1st time)", () -> System.out.println(i.get())),
            forEachOf(
                "a", "b", "c"
            ).sequentially(

            ).perform(
                j -> sequential(
                    sleep(1, MILLISECONDS),
                    simple("print i and j", () -> System.out.printf("  i=%s, j=%s%n", i.get(), j.get()))
                )
            ),
            simple("print the given value(2nd time)", () -> System.out.println(i.get()))
        )
    );
  }

  @Test
  public void runAction(Action action) {
    ActionRunner.WithResult runner = new ActionRunner.WithResult();
    try {
      action.accept(runner);
    } finally {
      action.accept(runner.createPrinter());
    }
  }
}
