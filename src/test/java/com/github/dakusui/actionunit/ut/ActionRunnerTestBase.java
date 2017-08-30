package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.utils.TestUtils;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public abstract class ActionRunnerTestBase<R extends Action.Visitor, P extends Action.Visitor> implements Context {
  private final TestUtils.Out out    = new TestUtils.Out();
  private final R  runner = createRunner();

  protected abstract R createRunner();

  public abstract P getPrinter(Writer writer);

  public P getPrinter() {
    return getPrinter(getWriter());
  }

  @SuppressWarnings("unchecked")
  public R getRunner() {
    return this.runner;
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
