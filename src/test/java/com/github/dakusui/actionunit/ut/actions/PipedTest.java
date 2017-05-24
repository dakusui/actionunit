package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.compat.CompatActions;
import com.github.dakusui.actionunit.compat.connectors.Sink;
import com.github.dakusui.actionunit.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import org.junit.Test;

import java.util.function.Function;

import static java.util.Arrays.asList;

public class PipedTest {
  @Test
  public void givenPipeInsideWith$whenPerformed() {
    Action action = CompatActions.with("Hello",
        CompatActions.pipe(
            (Function<String, Integer>) input -> input.length(),
            new Sink.Base<Integer>() {
              @Override
              protected void apply(Integer input, Object... outer) {
                System.out.println(input);
              }
            }
        )
    );
    action.accept(new ActionRunner.Impl());
  }

  @Test
  public void givenPipeInsideForEach$whenPerformed() {
    Action action = CompatActions.foreach(asList("Hello", "Hello1", "Hello12"),
        CompatActions.pipe(
            (Function<String, Integer>) input -> {
              System.out.println(input);
              return input.length();
            },
            new Sink.Base<Integer>() {
              @Override
              protected void apply(Integer input, Object... outer) {
                System.out.println("a" + input);
              }
            },
            new Sink.Base<Integer>() {
              @Override
              protected void apply(Integer input, Object... outer) {
                System.out.println("b" + input);
              }
            }
        )
    );
    ActionRunner.WithResult runner = new ActionRunner.WithResult();
    try {
      action.accept(runner);
    } finally {
      action.accept(runner.createPrinter(new TestUtils.Out()));
    }

  }

}
