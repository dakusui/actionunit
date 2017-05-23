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
public class ForEach2Example {
  @PerformWith(Test.class)
  public Action composeSingleLoop() {
    return foreach(
        value -> sequential(
            simple("print the given value(1st time)", () -> System.out.println(value.get())),
            simple("print the given value(2nd time)", () -> System.out.println(value.get()))
        ), ForEach.Mode.SEQUENTIALLY, asList("A", "B", "C")
    );
  }

  @PerformWith(Test.class)
  public Action composeNestedLoop() {
    return foreach(
        i -> sequential(
            simple("print the given value(1st time)", () -> System.out.println(i.get())),
            foreach(
                j -> sequential(
                    sleep(1, MILLISECONDS),
                    simple("print i and j", () -> System.out.printf("  i=%s, j=%s%n", i.get(), j.get()))
                ),
                ForEach.Mode.SEQUENTIALLY,
                asList("a", "b", "c")
            ),
            simple("print the given value(2nd time)", () -> System.out.println(i.get()))
        ), ForEach.Mode.SEQUENTIALLY, asList("A", "B", "C")
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
