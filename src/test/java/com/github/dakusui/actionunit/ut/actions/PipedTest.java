package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.actionunit.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import com.google.common.base.Function;
import org.junit.Test;

import static com.github.dakusui.actionunit.Actions.*;
import static java.util.Arrays.asList;

public class PipedTest {
  @Test
  public void givenPipeInsideWith$whenPerformed() {
    Action action = with("Hello",
        pipe(
            new Function<String, Integer>() {
              @Override
              public Integer apply(String input) {
                return input.length();
              }
            },
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
    Action action = foreach(asList("Hello", "Hello1", "Hello12"),
        pipe(
            new Function<String, Integer>() {
              @Override
              public Integer apply(String input) {
                System.out.println(input);
                return input.length();
              }
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
