package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.compat.visitors.CompatActionRunnerWithResult;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.compat.CompatActions;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.ActionUnit;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.github.dakusui.actionunit.helpers.Actions.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * An example to illustrate how to retry an action.
 */
@RunWith(ActionUnit.class)
public class RecoveryOperation {
  @ActionUnit.PerformWith({ Test.class })
  public Action deployment() {
    return CompatActions.attempt(deployComponent())
        .recover(
            CompatActions.retry(
                sequential(
                    cleanUp(),
                    deployComponent()),
                2,
                10, MILLISECONDS))
        .ensure(
            cleanUp())
        .build();
  }


  private Action deployComponent() {
    return named("deploy", simple(
        "This action succeeds after 2 tries",
        new Runnable() {
          int i = 0;

          @Override
          public void run() {
            if (i++ < 2) {
              throw new ActionException("Failed[" + i + "]");
            }
            System.out.println("Succeeded");
          }
        }));
  }

  private Action cleanUp() {
    return named("cleanUp", simple("Cleaning up", new Runnable() {
      @Override
      public void run() {
        System.out.println("Cleaning up");
      }
    }));
  }

  @Test
  public void runAction(Action action) {
    CompatActionRunnerWithResult runner = new CompatActionRunnerWithResult();
    action.accept(runner);
    action.accept(runner.createPrinter());
  }
}
