package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.ActionUnit;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.visitors.reporting.ReportingActionPerformer;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.github.dakusui.actionunit.core.ActionSupport.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * An example to illustrate how to retry an action.
 */
@RunWith(ActionUnit.class)
public class RecoveryOperation {
  @ActionUnit.PerformWith({ Test.class })
  public Action deployment() {
    return attempt(
        deployComponent()
    ).recover(
        Exception.class,
        ($, e) -> $.retry($.sequential(
            cleanUp($),
            deployComponent()
        )).times(2).withIntervalOf(10, MILLISECONDS).build()
    ).ensure(
        this::cleanUp
    );
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

  private Action cleanUp(Context $) {
    return $.named(
        "cleanUp",
        simple("Cleaning up",
            () -> System.out.println("Cleaning up")
        ));
  }

  @Test
  public void runAction(Action action) {
    new ReportingActionPerformer.Builder(action).build().performAndReport();
  }
}
