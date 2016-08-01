package com.github.dakusui.actionunit;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.internal.runners.statements.InvokeMethod;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Parameterized;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.LinkedList;
import java.util.List;

import static com.github.dakusui.actionunit.Utils.createTestClassMock;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Iterables.*;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class ActionUnit extends Parameterized {
  @Retention(RetentionPolicy.RUNTIME)
  public @interface PerformWith {
    Class<? extends Annotation>[] value() default { Test.class };
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

  @Override
  protected TestClass createTestClass(Class<?> testClass) {
    return createTestClassMock(super.createTestClass(testClass));
  }

  @Override
  public List<Runner> getChildren() {
    return this.runners;
  }

  private Iterable<Runner> createRunners() {
    return transform(
        collectActions(),
        new Function<Entry, Runner>() {
          @Override
          public Runner apply(Entry input) {
            try {
              return new CustomRunner(getTestClass().getJavaClass(), input.action, input.id, input.anns);
            } catch (InitializationError initializationError) {
              throw propagate(initializationError);
            }
          }
        }
    );
  }

  private Iterable<Entry> collectActions() {
    Iterable<Entry> ret = emptyList();
    for (FrameworkMethod each : getTestClass().getAnnotatedMethods(PerformWith.class)) {
      ret = concat(ret, createActions(each, size(ret)));
    }
    return ret;
  }

  private List<Entry> createActions(final FrameworkMethod testMethod, final int offset) {
    try {
      Object result = checkNotNull(testMethod.invokeExplosively(this.getTestClass().getJavaClass().newInstance()));
      if (result instanceof Action) {
        return singletonList(new Entry(offset, (Action) result, testMethod.getAnnotation(PerformWith.class).value()));
      }
      if (result.getClass().isArray() && Action.class.isAssignableFrom(result.getClass().getComponentType())) {
        final List<Action> actions = asList((Action[]) result);
        return Lists.transform(
            actions,
            new Function<Action, Entry>() {
              @Override
              public Entry apply(Action input) {
                return new Entry(
                    offset + actions.indexOf(input),
                    input,
                    testMethod.getAnnotation(PerformWith.class).value()
                );
              }
            });
      }
      throw new RuntimeException(format("Unsupported type (%s)", result.getClass().getCanonicalName()));
    } catch (Throwable e) {
      throw propagate(e);
    }
  }

  private static class Entry {
    final int                           id;
    final Action                        action;
    final Class<? extends Annotation>[] anns;

    private Entry(int id, Action action, Class<? extends Annotation>[] anns) {
      this.id = id;
      this.action = action;
      this.anns = anns;
    }
  }

  private static class CustomRunner extends BlockJUnit4ClassRunner {
    private final Action                        action;
    private final int                           id;
    private final Class<? extends Annotation>[] anns;

    /**
     * Creates a BlockJUnit4ClassRunner to run {@code klass}
     *
     * @throws InitializationError if the test class is malformed.
     */
    CustomRunner(Class<?> testClass, Action action, int id, Class<? extends Annotation>[] anns) throws InitializationError {
      super(testClass);
      this.action = checkNotNull(action);
      this.id = id;
      this.anns = checkNotNull(anns);
    }

    @Override
    public Object createTest() throws Exception {
      return getTestClass().getOnlyConstructor().newInstance();
    }

    @Override
    protected List<FrameworkMethod> computeTestMethods() {
      List<FrameworkMethod> ret = new LinkedList<>();
      for (Class<? extends Annotation> each : anns) {
        ret.addAll(getTestClass().getAnnotatedMethods(each));
      }
      return ret;
    }

    /**
     * Returns a {@link Statement} that invokes {@code method} on {@code test}
     */
    @Override
    protected Statement methodInvoker(final FrameworkMethod method, final Object test) {
      return new InvokeMethod(method, test) {
        @Override
        public void evaluate() throws Throwable {
          method.invokeExplosively(test, action);
        }
      };
    }

    @Override
    protected Description describeChild(FrameworkMethod method) {
      return Description.createTestDescription(
          getTestClass().getJavaClass(),
          testName(method),
          method.getAnnotations()
      );
    }

    @Override
    protected String getName() {
      return format("[%d]", this.id);
    }

    @Override
    protected String testName(FrameworkMethod method) {
      return format("%s[%d]", method.getName(), this.id);
    }

    @Override
    protected void collectInitializationErrors(List<Throwable> errors) {
      ////
      // TODO Implement validations here.
    }

    @Override
    protected Annotation[] getRunnerAnnotations() {
      return new Annotation[0];
    }
  }
}
