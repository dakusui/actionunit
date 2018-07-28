package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.compat.actions.ValueHolder;
import com.github.dakusui.actionunit.compat.core.Action;
import com.github.dakusui.actionunit.compat.core.Context;
import com.github.dakusui.actionunit.compat.visitors.reporting.ReportingActionPerformer;
import org.junit.Test;

import static java.util.Arrays.asList;

public class Example implements UtContext {
  @Test
  public void test() {
    Action action = forEachOf(
        asList("hello", "world")
    ).concurrently(
    ).perform(
        (ValueHolder<String> v) -> (Context f) -> f.sequential(
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
