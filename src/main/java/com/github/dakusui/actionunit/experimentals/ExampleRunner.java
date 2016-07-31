package com.github.dakusui.actionunit.experimentals;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.Actions;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.util.Arrays;
import java.util.List;

public class ExampleRunner extends ParentRunner<Action> {
  /**
   * Constructs a new {@code ParentRunner} that will run {@code @TestClass}
   *
   * @param testClass
   */
  public ExampleRunner(Class<?> testClass) throws InitializationError {
    super(testClass);
  }

  @Override
  protected List<Action> getChildren() {
    return Arrays.asList(
        Actions.simple("test1", new Runnable() {
          @Override
          public void run() {
            System.out.println("test1");
          }
        }),
        Actions.simple("test2", new Runnable() {
          @Override
          public void run() {
            System.out.println("test2-0");
          }
        }),
        Actions.simple("test2", new Runnable() {
          @Override
          public void run() {
            System.out.println("test2-1");
          }
        })
    );
  }

  @Override
  protected Description describeChild(Action child) {
    return Description.createTestDescription(this.getTestClass().getJavaClass(), child.format());
  }

  @Override
  protected void runChild(Action child, RunNotifier notifier) {
    runLeaf(actionBlock(child), describeChild(child), notifier);
  }

  private Statement actionBlock(final Action child) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        child.accept(new Action.Visitor.Impl());
      }
    };
  }
}
