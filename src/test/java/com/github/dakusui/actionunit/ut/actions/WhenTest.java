package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.compat.CompatActions;
import com.github.dakusui.actionunit.compat.connectors.Sink;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import org.junit.Test;

import static com.github.dakusui.actionunit.Actions.*;
import static java.util.Arrays.asList;

public class WhenTest {
  @Test
  public void test() {
    Action action = CompatActions.foreach(
        asList(1, 2, 3, 4),
        when(
            (Integer input) -> input > 2,
            tag(0)
        ),
        new Sink.Base<Integer>() {
          @Override
          protected void apply(Integer input, Object... outer) {
            System.out.println("hello" + input);
          }
        }
    );

    ActionRunner.WithResult runner = new ActionRunner.WithResult();
    try {
      action.accept(runner);
    } finally {
      action.accept(runner.createPrinter());
    }
  }
}
