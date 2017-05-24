package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.compat.visitors.CompatActionRunnerWithResult;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.compat.CompatActions;
import com.github.dakusui.actionunit.compat.connectors.Sink;
import org.junit.Test;

import static java.util.Arrays.asList;

public class WhenTest {
  @Test
  public void test() {
    Action action = CompatActions.foreach(
        asList(1, 2, 3, 4),
        CompatActions.when(
            (Integer input) -> input > 2,
            CompatActions.tag(0)
        ),
        new Sink.Base<Integer>() {
          @Override
          protected void apply(Integer input, Object... outer) {
            System.out.println("hello" + input);
          }
        }
    );

    CompatActionRunnerWithResult runner = new CompatActionRunnerWithResult();
    try {
      action.accept(runner);
    } finally {
      action.accept(runner.createPrinter());
    }
  }
}
