package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.ActionException;
import com.github.dakusui.actionunit.ActionUnit;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.github.dakusui.actionunit.Actions.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * An example to illustrate how to retry an action.
 */
@RunWith(ActionUnit.class)
public class RecoveryOperation {
  @ActionUnit.PerformWith({ Test.class })
  public Action deployment() {
    return attempt(deployComponent())
        .recover(
            retry(
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
    ActionRunner.WithResult runner = new ActionRunner.WithResult();
    action.accept(runner);
    action.accept(runner.createPrinter());
  }
}
