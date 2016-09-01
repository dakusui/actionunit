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

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import static com.github.dakusui.actionunit.Actions.named;
import static com.github.dakusui.actionunit.Utils.createTestClassMock;
import static com.github.dakusui.actionunit.Utils.isGivenTypeExpected_ArrayOfExpected_OrIterable;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Iterables.*;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * The custom runner of ActionUnit.
 */
public class ActionUnit extends Parameterized {
  /**
   * An annotation to let ActionUnit know the target elements annotated by it provide
   * actions executed by test methods.
   */
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface PerformWith {
    Class<? extends Annotation>[] value() default { Test.class };
  }

  /**
   * Test runners each of which runs a test case represented by an action.
   */
  private final List<Runner> runners;

  /**
   * Only called reflectively. Do not use programmatically.
   *
   * @param klass test target class
   */
  public ActionUnit(Class<?> klass) throws Throwable {
    super(klass);
    try {
      runners = asList(toArray(createRunners(), Runner.class));
    } catch (RuntimeException e) {
      if (e.getCause() instanceof InitializationError) {
        throw e.getCause();
      }
      throw e;
    }
  }

  @Override
  protected void collectInitializationErrors(List<Throwable> errors) {
    super.collectInitializationErrors(errors);
    this.validateActionMethods(errors);
  }

  @Override
  protected TestClass createTestClass(Class<?> testClass) {
    return createTestClassMock(super.createTestClass(testClass));
  }

  @Override
  public List<Runner> getChildren() {
    return this.runners;
  }

  protected void validateActionMethods(List<Throwable> errors) {
    List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(PerformWith.class);
    for (FrameworkMethod eachMethod : methods) {
      validateNonStaticPublic(eachMethod, errors);
      validateActionReturned(eachMethod, errors);
      validateNoParameter(eachMethod, errors);
    }
  }

  private void validateNoParameter(FrameworkMethod method, List<Throwable> errors) {
    if (method.getMethod().getParameterTypes().length != 0) {
      errors.add(new Exception("Method " + method.getName() + "(...) must have no parameters"));
    }
  }

  private void validateNonStaticPublic(FrameworkMethod method, List<Throwable> errors) {
    if (method.isStatic()) {
      errors.add(new Exception("Method " + method.getName() + "() must not be static"));
    }
    if (!method.isPublic()) {
      errors.add(new Exception("Method " + method.getName() + "() must be public"));
    }
  }

  private void validateActionReturned(FrameworkMethod method, List<Throwable> errors) {
    if (!Action.class.isAssignableFrom(method.getType())
        && !isGivenTypeExpected_ArrayOfExpected_OrIterable(Action.class, method.getReturnType())) {
      errors.add(new Exception("Method " + method.getName() + "() must return Action, its array, or its iterable"));
    }
  }


  private Iterable<Runner> createRunners() {
    return transform(
        collectActions(),
        new Function<Entry, Runner>() {
          @Override
          public Runner apply(final Entry input) {
            try {
              return new CustomRunner(getTestClass().getJavaClass(), input.action, input.id) {
                @Override
                protected List<FrameworkMethod> computeTestMethods() {
                  List<FrameworkMethod> ret = new LinkedList<>();
                  for (Class<? extends Annotation> each : input.anns) {
                    ret.addAll(getTestClass().getAnnotatedMethods(each));
                  }
                  return ret;
                }

                @Override
                protected Class<? extends Annotation>[] getAnnotationsForTestMethods() {
                  return input.anns;
                }
              };
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
        return singletonList(new Entry(
            offset,
            named(testMethod.getName(), (Action) result),
            testMethod.getAnnotation(PerformWith.class).value())
        );
      }
      if (isGivenTypeExpected_ArrayOfExpected_OrIterable(Action.class, result.getClass())) {
        final List<Action> actions;
        if (result.getClass().isArray()) {
          actions = asList((Action[]) result);
        } else {
          //noinspection unchecked
          actions = Lists.newLinkedList((Iterable<? extends Action>) result);
        }
        return Lists.transform(
            actions,
            new Function<Action, Entry>() {
              @Override
              public Entry apply(Action input) {
                int index = actions.indexOf(input);
                return new Entry(
                    offset + index,
                    named(format("%s[%s]", testMethod.getName(), index), input),
                    testMethod.getAnnotation(PerformWith.class).value()
                );
              }
            });
      }
      throw new RuntimeException(format("Unsupported type (%s)", result.getClass().getCanonicalName()));
    } catch (IllegalAccessException | InstantiationException e) {
      throw ActionException.wrap(e);
    } catch (Throwable throwable) {
      throw new ActionException(throwable);
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

  private static abstract class CustomRunner extends BlockJUnit4ClassRunner {
    private final Action action;
    private final int    id;

    /**
     * Creates a BlockJUnit4ClassRunner to run {@code klass}
     *
     * @throws InitializationError if the test class is malformed.
     */
    CustomRunner(Class<?> testClass, Action action, int id) throws InitializationError {
      super(testClass);
      this.action = checkNotNull(action);
      this.id = id;
    }

    @Override
    protected void validateTestMethods(List<Throwable> errors) {
      for (Class<? extends Annotation> each : getAnnotationsForTestMethods()) {
        List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(each);
        for (FrameworkMethod eachTestMethod : methods) {
          eachTestMethod.validatePublicVoid(false, errors);
          validateOnlyOneParameter(eachTestMethod, errors);
        }
      }
    }

    private void validateOnlyOneParameter(FrameworkMethod testMethod, List<Throwable> errors) {
      Method method = testMethod.getMethod();
      if (method.getParameterTypes().length == 0) {
        errors.add(new Exception("Method " + method.getName() + "() should have one and only one parameter"));
        return;
      }
      if (method.getParameterTypes().length > 1) {
        errors.add(new Exception("Method " + method.getName() + "() should have only one parameter"));
      }
      if (!Action.class.isAssignableFrom(method.getParameterTypes()[0])) {
        errors.add(new Exception("Method " + method.getName() + "()'s 1 st parameter must accept an Action"));
      }
    }

    @Override
    public Object createTest() throws Exception {
      return getTestClass().getOnlyConstructor().newInstance();
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
    protected Annotation[] getRunnerAnnotations() {
      return new Annotation[0];
    }

    protected abstract Class<? extends Annotation>[] getAnnotationsForTestMethods();
  }
}
