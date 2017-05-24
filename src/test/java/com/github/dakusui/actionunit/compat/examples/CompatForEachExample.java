package com.github.dakusui.actionunit.compat.examples;

import com.github.dakusui.actionunit.compat.visitors.CompatActionRunnerWithResult;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.ActionUnit;
import com.github.dakusui.actionunit.compat.CompatActions;
import com.github.dakusui.actionunit.compat.Context;
import com.github.dakusui.actionunit.compat.connectors.Sink;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.github.dakusui.actionunit.helpers.Actions.*;
import static java.util.Arrays.asList;

@RunWith(ActionUnit.class)
public class
CompatForEachExample {
  @ActionUnit.PerformWith(Test.class)
  public Action compose() {
    return CompatActions.foreach(
        asList("A", "B", "C"),
        sequential(
            CompatActions.sink(new Sink<String>() {
              @Override
              public void apply(String input, Context context) {
                System.out.println("input=" + input);
              }
            }),
            CompatActions.sink(new Sink<String>() {
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
    CompatActionRunnerWithResult runner = new CompatActionRunnerWithResult();
    try {
      action.accept(runner);
    } finally {
      action.accept(runner.createPrinter());
    }
  }
}
