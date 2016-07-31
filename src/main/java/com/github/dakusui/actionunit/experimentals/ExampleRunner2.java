package com.github.dakusui.actionunit.experimentals;

import com.google.common.base.Throwables;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ExampleRunner2 extends Suite {
  public ExampleRunner2(Class<?> klass) throws InitializationError {
    super(klass, Collections.<Runner>emptyList());
  }

  @Override
  protected List<Runner> getChildren() {
    try {
      return Arrays.<Runner>asList(new ParentRunner<Runner>(Example.class) {
        @Override
        protected List<Runner> getChildren() {
          return Arrays.<Runner>asList(
              new Runner() {
                @Override
                public Description getDescription() {
                  return Description.createTestDescription(Example.class, "test3");
                }

                @Override
                public void run(RunNotifier notifier) {
                  Description description = getDescription();
                  notifier.fireTestStarted(description);
                  notifier.fireTestFinished(description);
                }
              },
              new Runner() {
                @Override
                public Description getDescription() {
                  return Description.createTestDescription(Example.class, "test3");
                }

                @Override
                public void run(RunNotifier notifier) {
                  Description description = getDescription();
                  notifier.fireTestStarted(description);
                  notifier.fireTestFinished(description);
                }
              }
          );
        }

        @Override
        protected Description describeChild(Runner child) {
          return Description.createTestDescription(Example.class, "test4");
        }

        @Override
        protected void runChild(Runner child, RunNotifier notifier) {
          child.run(notifier);
        }
      }, new ExampleRunner(Example.class));
    } catch (InitializationError initializationError) {
      throw Throwables.propagate(initializationError);
    }
  }
}
