package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.actions.ValueHolder;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.visitors.reporting.ReportingActionPerformer;
import org.junit.Test;

import static java.util.Arrays.asList;

public class Example implements Context {
  @Test
  public void test() {
    Action action = forEachOf(
        asList("hello", "world")
    ).concurrently(
    ).perform(
        (Context f, ValueHolder<String> v) -> f.sequential(
            f.simple("print", () -> System.out.println(v.get())),
            f.simple("print", () -> System.out.println(v.get())),
            f.sequential(
                f.simple("print", () -> System.out.println(v.get())),
                f.simple("print", () -> System.out.println(v.get()))
            )
        )
    );

    new ReportingActionPerformer.Builder(action).build().performAndReport();
  }
}
