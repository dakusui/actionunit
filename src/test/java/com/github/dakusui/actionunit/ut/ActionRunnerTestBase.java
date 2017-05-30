package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.ActionPrinter;
import com.github.dakusui.actionunit.visitors.ReportingActionRunner;

import java.util.concurrent.TimeUnit;

import static com.github.dakusui.actionunit.helpers.Actions.named;
import static com.github.dakusui.actionunit.helpers.Actions.simple;
import static org.junit.Assert.assertTrue;

public abstract class ActionRunnerTestBase {
  private final TestUtils.Out out    = new TestUtils.Out();
  private final Action.Visitor  runner = createRunner();

  protected abstract Action.Visitor createRunner();

  public abstract ActionPrinter getPrinter(ReportingActionRunner.Writer writer);

  public ActionPrinter getPrinter() {
    return getPrinter(getWriter());
  }

  @SuppressWarnings("unchecked")
  public <A extends Action.Visitor> A getRunner() {
    return (A) this.runner;
  }

  public TestUtils.Out getWriter() {
    return this.out;
  }

  public Action createPassingAction(int index, final int durationInMilliseconds) {
    return named(
        String.format("A passing action-%d", index),
        simple(String.format("This passes always-%d", index), () -> {
          try {
            TimeUnit.MICROSECONDS.sleep(durationInMilliseconds);
          } catch (InterruptedException e) {
            throw ActionException.wrap(e);
          }
        }));
  }

  public Action createPassingAction(final int durationInMilliseconds) {
    return named("A passing action", simple("This passes always", new Runnable() {
      @Override
      public void run() {
        try {
          TimeUnit.MICROSECONDS.sleep(durationInMilliseconds);
        } catch (InterruptedException e) {
          throw ActionException.wrap(e);
        }
      }
    }));
  }

  public Action createPassingAction() {
    return createPassingAction(0);
  }

  public Action createFailingAction() {
    return named(
        "A failing action",
        simple(
            "This fails always",
            () -> assertTrue("Expected failure", false))
    );
  }

  public Action createErrorAction() {
    return named("An error action",
        simple("This gives a runtime exception always", new Runnable() {
          @Override
          public void run() {
            throw new RuntimeException("Expected runtime exception");
          }
        }));
  }
}
