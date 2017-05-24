package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.ActionPrinter;
import com.github.dakusui.actionunit.visitors.ActionRunner;

import java.util.concurrent.TimeUnit;

import static com.github.dakusui.actionunit.helpers.Actions.named;
import static com.github.dakusui.actionunit.helpers.Actions.simple;
import static org.junit.Assert.assertTrue;

public abstract class ActionRunnerTestBase {
  private final TestUtils.Out out    = new TestUtils.Out();
  private final ActionRunner  runner = createRunner();

  protected abstract ActionRunner createRunner();

  public abstract ActionPrinter.Impl getPrinter(ActionPrinter.Impl.Writer writer);

  public ActionPrinter.Impl getPrinter() {
    return getPrinter(getWriter());
  }

  public <A extends ActionRunner> A getRunner() {
    return (A) this.runner;
  }

  public TestUtils.Out getWriter() {
    return this.out;
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
