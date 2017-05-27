package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.ActionUnit;
import com.github.dakusui.actionunit.ActionUnit.PerformWith;
import com.github.dakusui.actionunit.compat.visitors.CompatActionRunnerWithResult;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.helpers.Actions2;
import com.github.dakusui.actionunit.helpers.Builders2;
import com.github.dakusui.actionunit.visitors.*;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@RunWith(ActionUnit.class)
public class ForEachExample implements Actions2, Builders2 {
  @PerformWith(Test.class)
  public Action composeSingleLoop() {
    AtomicInteger c = new AtomicInteger(0);
    return forEachOf(
        asList("A", "B", "C")
    ).sequentially(
    ).perform(
        value -> sequential(
            simple("print the given value(1st time)", () -> System.out.println(value.get())),
            simple("print the given value(2nd time)", () -> {
              if (c.getAndIncrement() > 1)
                throw new RuntimeException();
              System.out.println(value.get());
            }),
            simple("print the given value(3rd time)", () -> System.out.println(value.get()))
        )
    );
  }

  @PerformWith(Test.class)
  public Action composeSingleLoop2() {
    return sequential(
        simple("print hello", () -> System.out.println("hello")),
        forEachOf(
            asList("A", "B", "C")
        ).sequentially(
        ).perform(
            value -> sequential(
                simple("print the given value(1st time)", () -> System.out.println(value.get())),
                simple("print the given value(2nd time)", () -> System.out.println(value.get()))
            )
        ),
        simple("print bye", () -> System.out.println("bye"))
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
            ).sequentially().perform(
                j -> sequential(
                    sleep(1, MILLISECONDS),
                    simple("print i and j", () -> System.out.printf("  i=%s, j=%s%n", i.get(), j.get()))
                )
            ),
            simple("print the given value(2nd time)", () -> System.out.println("END:" + i.get()))
        )
    );
  }

  @Ignore
  @Test
  public void runAction(Action action) {
    CompatActionRunnerWithResult runner = new CompatActionRunnerWithResult();
    try {
      action.accept(runner);
    } finally {
      action.accept(ActionPrinter.Factory.DEFAULT_INSTANCE.stdout());
    }
  }

  @Test
  public void runAction2(Action action) {
    new ActionReporter.Builder(action)
        .with(Report.Record.Formatter.DEFAULT_INSTANCE)
        .to(ActionPrinter.Writer.Std.ERR)
        .build()
        .perform();
  }


  @Ignore
  @Test
  public void compatRunAction(Action action) {
    CompatActionRunnerWithResult runner = new CompatActionRunnerWithResult();
    try {
      action.accept(runner);
    } finally {
      action.accept(runner.createPrinter());
    }
  }

  @Ignore
  @Test
  public void buildTree(Action action) {
    System.out.println("-->" + TreeBuilder.traverse(action).format());
    System.out.println("----");
    Node.print(TreeBuilder.traverse(action), System.out);
    System.out.println("----");
  }
}
