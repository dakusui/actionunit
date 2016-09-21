package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.ActionUnit;
import com.github.dakusui.actionunit.Context;
import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.github.dakusui.actionunit.Actions.*;
import static java.util.Arrays.asList;

@RunWith(ActionUnit.class)
public class ForEachExample {
  @ActionUnit.PerformWith(Test.class)
  public Action compose() {
    return foreach(
        asList("A", "B", "C"),
        sequential(
            sink(new Sink<String>() {
              @Override
              public void apply(String input, Context context) {
                System.out.println("input=" + input);
              }
            }),
            sink(new Sink<String>() {
              @Override
              public void apply(String input, Context context) {
                System.out.println("  context=" + (context == null ? null : context.value()));
              }
            })
        )
    );
  }

  @Test
  public void runAction(Action action) {
    ActionRunner.WithResult runner = new ActionRunner.WithResult();
    try {
      action.accept(runner);
    } finally {
      action.accept(runner.createPrinter());
    }
  }
}
