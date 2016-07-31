package com.github.dakusui.actionunit;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.junit.Test;
import org.junit.internal.runners.statements.InvokeMethod;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Parameterized;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Iterables.toArray;
import static java.lang.String.format;
import static java.util.Arrays.asList;

public class ActionUnit extends Parameterized {
  static class CustomRunner extends BlockJUnit4ClassRunner {
    private final String name;
    private final Action action;

    /**
     * Creates a BlockJUnit4ClassRunner to run {@code klass}
     *
         * @throws InitializationError if the test class is malformed.
     */
    public CustomRunner(Class<?> testClass, String testName, Action action) throws InitializationError {
      super(testClass);
      this.name = testName;
      this.action = action;
    }

    @Override
    public Object createTest() throws Exception {
      return getTestClass().getOnlyConstructor().newInstance();
    }

    /**
     * Returns a {@link Statement} that invokes {@code method} on {@code test}
     */
    protected Statement methodInvoker(final FrameworkMethod method, final Object test) {
      return new InvokeMethod(method, test) {
        @Override
        public void evaluate() throws Throwable {
          method.invokeExplosively(test, action);
        }
      };
    }

    @Override
    protected String getName() {
      return this.name;
    }

    @Override
    protected String testName(FrameworkMethod method) {
      return method.getName() + getName();
    }

    @Override
    protected void collectInitializationErrors(List<Throwable> errors) {
      ////
      // TODO Implement validations here.
    }

    @Override
    protected Statement classBlock(RunNotifier notifier) {
      return childrenInvoker(notifier);
    }

    @Override
    protected Annotation[] getRunnerAnnotations() {
      return new Annotation[0];
    }
  }

  private final List<Runner> runners;

  /**
   * Only called reflectively. Do not use programmatically.
   *
   * @param klass test target class
   */
  public ActionUnit(Class<?> klass) throws Throwable {
    super(klass);
    runners = asList(toArray(createRunners(), Runner.class));
  }

  /**
   * Mock {@code Parameterized} runner of JUnit 4.12.
   */
  @Override
  protected TestClass createTestClass(Class<?> clazz) {
    return new TestClass(clazz) {
      public List<FrameworkMethod> getAnnotatedMethods(
          Class<? extends Annotation> annotationClass) {
        if (Parameterized.Parameters.class.equals(annotationClass)) {
          return new TestClass(DummyMethodHolderForParameterizedRunner.class).getAnnotatedMethods(Parameterized.Parameters.class);
        }
        return super.getAnnotatedMethods(annotationClass);
      }
    };
  }

  public List<Runner> getChildren() {
    return this.runners;
  }

  private Iterable<Runner> createRunners() {
    return Iterables.transform(
        collectActions(),
        new Function<Action, Runner>() {
          @Override
          public CustomRunner apply(Action action) {
            try {
              return new CustomRunner(getTestClass().getJavaClass(), action.format(), action);
            } catch (InitializationError initializationError) {
              throw propagate(initializationError);
            }
          }
        }
    );
  }

  @Override
  protected void collectInitializationErrors(List<Throwable> errors) {
    ////
    // TODO Implement validations here.
  }

  private Iterable<Action> collectActions() {
    Iterable<Action> ret = Collections.emptyList();
    for (FrameworkMethod each : getTestClass().getAnnotatedMethods(Test.class)) {
      ret = Iterables.concat(ret, createActions(each));
    }
    return ret;
  }

  private List<Action> createActions(FrameworkMethod testMethod) {
    try {
      Object result = checkNotNull(testMethod.invokeExplosively(this.getTestClass().getJavaClass().newInstance()));
      if (result instanceof Action) {
        return Collections.singletonList((Action) result);
      }
      if (result.getClass().isArray() && Action.class.isAssignableFrom(result.getClass().getComponentType())) {
        return asList((Action[]) result);
      }
      throw new RuntimeException(format("Unsupported type (%s)", result.getClass().getCanonicalName()));
    } catch (Throwable e) {
      throw propagate(e);
    }
  }

  /**
   * A class referenced by createTestClass method.
   * This is only used to mock JUnit's Parameterized runner.
   */
  public static class DummyMethodHolderForParameterizedRunner {
    @SuppressWarnings("unused") // This method is referenced reflectively.
    @Parameters
    public static Object[][] dummy() {
      return new Object[][] { {} };
    }
  }

}
