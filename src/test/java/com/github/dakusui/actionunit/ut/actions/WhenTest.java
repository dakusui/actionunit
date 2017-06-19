package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.visitors.reporting.ReportingActionPerformer;
import org.junit.Test;

import static java.util.Arrays.asList;

public class WhenTest implements Context {
  @Test
  public void test() {
    Action action = forEachOf(
        asList(1, 2, 3, 4)
    ).perform(
        ($, v) -> $.when(
            v,
            (Integer input) -> input > 2
        ).perform(
            ($$) -> $$.simple(
                "hello",
                () -> System.out.println("hello" + v.get())
            )
        ).$()
    );
    new ReportingActionPerformer.Builder(action).build().performAndReport();
  }
}
