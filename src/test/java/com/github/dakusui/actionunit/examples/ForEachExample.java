package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.ActionUnit;
import com.github.dakusui.actionunit.ActionUnit.PerformWith;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.helpers.Actions2;
import com.github.dakusui.actionunit.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.reporting.ActionTreeBuilder;
import com.github.dakusui.actionunit.visitors.reporting.Node;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@RunWith(ActionUnit.class)
public class ForEachExample extends TestUtils.TestBase implements Actions2 {
  @PerformWith(Test.class)
  public Action composeSingleLoop() {
    return forEachOf(
        asList("A", "B", "C")
    ).sequentially(
    ).perform(
        value -> sequential(
            simple("print the given value(1st time)", () -> System.out.println(value.get())),
            simple("print the given value(2nd time)", () -> System.out.println(value.get())),
            simple("print the given value(3rd time)", () -> System.out.println(value.get()))
        )
    );
  }

  @PerformWith(Test.class)
  public Action composeSingleLoopWithWhenClause() {
    AtomicInteger c = new AtomicInteger(0);
    return forEachOf(
        asList("A", "B", "C")
    ).sequentially(
    ).perform(
        (Supplier<String> value) -> sequential(
            when(value, "C"::equals)
                .perform(
                    v -> simple("print to stderr", () -> System.err.println(v.get()))
                )
                .otherwise(
                    v -> simple("print to stdout", () -> System.out.println(v.get()))
                ),
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
                simple("print the given value(2nd time)", () -> System.out.println(value.get())),
                sleep(2, MICROSECONDS)
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

  @Test
  public void runAction2(Action action) {
    TestUtils.createReportingActionPerformer(action).perform();
  }

  @Test
  public void buildTree(Action action) {
    System.out.println("-->" + ActionTreeBuilder.traverse(action).format());
    System.out.println("----");
    Node.print(ActionTreeBuilder.traverse(action), System.out);
    System.out.println("----");
  }
}
