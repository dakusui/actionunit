package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.ActionPrinter;
import com.github.dakusui.actionunit.visitors.ActionRunner;

import java.util.concurrent.TimeUnit;

import static com.github.dakusui.actionunit.Actions.simple;
import static org.junit.Assert.assertTrue;

abstract class ActionRunnerTestBase {
  private final TestUtils.Out out    = new TestUtils.Out();
  private final ActionRunner  runner = createRunner();

  protected abstract ActionRunner createRunner();

  public abstract ActionPrinter getPrinter(ActionPrinter.Writer writer);

  public ActionPrinter getPrinter() {
    return getPrinter(getWriter());
  }

  public <A extends ActionRunner> A getRunner() {
    return (A) this.runner;
  }

  public TestUtils.Out getWriter() {
    return this.out;
  }

  public Action createPassingAction(final int durationInMilliseconds) {
    return simple("A passing action", new Runnable() {
      @Override
      public void run() {
        try {
          TimeUnit.MICROSECONDS.sleep(durationInMilliseconds);
        } catch (InterruptedException e) {
          throw ActionException.wrap(e);
        }
      }

      @Override
      public String toString() {
        return "This passes always";
      }
    });
  }

  public Action createPassingAction() {
    return createPassingAction(0);
  }

  public Action createFailingAction() {
    return simple("A failing action", new Runnable() {
      @Override
      public void run() {
        assertTrue("Expected failure", false);
      }

      @Override
      public String toString() {
        return "This fails always";
      }
    });
  }

  public Action createErrorAction() {
    return simple("An error action", new Runnable() {
      @Override
      public void run() {
        throw new RuntimeException("Expected runtime exception");
      }

      @Override
      public String toString() {
        return "This gives a runtime exception always";
      }
    });
  }
}
