package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.helpers.Actions2;
import com.github.dakusui.actionunit.helpers.Builders2;
import com.github.dakusui.actionunit.visitors.reporting.ReportingActionRunner;
import org.junit.Test;

import static java.util.Arrays.asList;

public class WhenTest implements Actions2, Builders2 {
  @Test
  public void test() {
    Action action = forEachOf(
        asList(1, 2, 3, 4)
    ).perform(
        v ->
            when(
                v,
                (Integer input) -> input > 2
            ).perform(
                u -> simple(
                    "hello",
                    () -> System.out.println("hello" + u.get())
                )
            ).$()
    );
    new ReportingActionRunner.Builder(action).build().perform();
  }
}
