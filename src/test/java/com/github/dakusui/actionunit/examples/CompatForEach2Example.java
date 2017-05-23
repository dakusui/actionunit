package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.ActionUnit;
import com.github.dakusui.actionunit.ActionUnit.PerformWith;
import com.github.dakusui.actionunit.actions.ForEach;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.github.dakusui.actionunit.Actions.*;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@RunWith(ActionUnit.class)
public class CompatForEach2Example {
  @PerformWith(Test.class)
  public Action composeSingleLoop() {
    return foreach2(
        asList("A", "B", "C"),
        ForEach.Mode.SEQUENTIALLY,
        value -> sequential(
            simple("print the given value(1st time)", () -> System.out.println(value.get())),
            simple("print the given value(2nd time)", () -> System.out.println(value.get()))
        )
    );
  }

  @PerformWith(Test.class)
  public Action composeNestedLoop() {
    return foreach2(
        asList("A", "B", "C"),
        ForEach.Mode.SEQUENTIALLY,
        i -> sequential(
            simple("print the given value(1st time)", () -> System.out.println(i.get())),
            foreach2(
                asList("a", "b", "c"),
                ForEach.Mode.SEQUENTIALLY,
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
