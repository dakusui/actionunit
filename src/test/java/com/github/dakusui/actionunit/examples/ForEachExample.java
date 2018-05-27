package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.ActionUnit;
import com.github.dakusui.actionunit.ActionUnit.PerformWith;
import com.github.dakusui.actionunit.actions.ValueHolder;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.reporting.ActionTreeBuilder;
import com.github.dakusui.actionunit.visitors.reporting.Node;
import com.github.dakusui.actionunit.visitors.reporting.ReportingActionPerformer;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@RunWith(ActionUnit.class)
public class ForEachExample extends TestUtils.TestBase implements Context {
  @PerformWith(Test.class)
  public Action composeSingleLoop() {
    return forEachOf(
        asList("A", "B", "C")
    ).sequentially(
    ).perform(
        ($, value) -> $.sequential(
            $.simple("print the given value(1st time)", () -> System.out.println(value.get())),
            $.simple("print the given value(2nd time)", () -> System.out.println(value.get())),
            $.simple("print the given value(3rd time)", () -> System.out.println(value.get()))
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
        (Context $, ValueHolder<String> value) -> $.sequential(
            $.when(value, "C"::equals)
                .perform(
                    ($$) -> $$.simple("print to stderr", () -> System.err.println(value.get()))
                )
                .otherwise(
                    ($$) -> $$.simple("print to stdout", () -> System.out.println(value.get()))
                ),
            $.simple("print the given value(1st time)", () -> System.out.println(value.get())),
            $.simple("print the given value(2nd time)", () -> {
              if (c.getAndIncrement() > 1)
                throw new RuntimeException();
              System.out.println(value.get());
            }),
            $.simple("print the given value(3rd time)", () -> System.out.println(value.get()))
        )
    );
  }

  @PerformWith(Test.class)
  public Action composeSingleLoop2() {
    return sequential(
        simple("print hello", () -> System.out.println("hello")),
        forEachOf(
            asList("A", "B", "C")
        ).withDefault(
            "unknown"
        ).concurrently(
        ).perform(
            (Context $, ValueHolder<String> value) -> {
              String v = value.isPresent() ?
                  value.get() :
                  String.format("(%s)", value.get());
              return $.sequential(
                  $.simple("print the given value(1st time)", () -> System.out.println(v)),
                  $.simple("print the given value(2nd time)", () -> System.out.println(v)),
                  $.sleep(2, MICROSECONDS)
              );
            }
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
        ($, i) -> $.sequential(
            $.simple("print the given value(1st time)", () -> System.out.println("BEGIN:" + i.get())),
            $.forEachOf(
                "a", "b", "c"
            ).sequentially().perform(
                ($$, j) -> $$.sequential(
                    $$.sleep(1, MILLISECONDS),
                    $$.simple("print i and j", () -> System.out.printf("  i=%s, j=%s%n", i.get(), j.get()))
                )
            ),
            $.simple("print the given value(2nd time)", () -> System.out.println("END:" + i.get()))
        )
    );
  }

  @Test
  public void runActionAndReportResult(Action action) {
    new ReportingActionPerformer.Builder(action).build().performAndReport();
  }

  @Test
  public void simpleRunAction(Action action) {
    action.accept(TestUtils.createActionPerformer());
  }

  @Test
  public void printOnly(Action action) {
    Node.print(ActionTreeBuilder.traverse(action), Writer.Std.OUT);
  }

  @Override
  public <V> V set(String variableName, V value) {
    return null;
  }

  @Override
  public <V> V get(String variableName) {
    return null;
  }
}
