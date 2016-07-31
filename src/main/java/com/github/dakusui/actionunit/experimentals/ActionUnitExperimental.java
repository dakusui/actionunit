package com.github.dakusui.actionunit.experimentals;

import com.github.dakusui.actionunit.Action;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import org.junit.runner.Description;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

public class ActionUnitExperimental extends BlockJUnit4ClassRunner {
  public @interface Config {
    Class<? extends Action.Visitor.Provider>[] actionVisitorProviders() default {Action.Visitor.Provider.ForDefaultRunner.class};
  }
  private final ConcurrentHashMap<Method, Description> methodDescriptions = new ConcurrentHashMap<>();

  private final Action.Visitor visitor;

  /**
   * Creates a BlockJUnit4ClassRunner to run {@code klass}
   *
   * @param klass
   * @throws InitializationError if the test class is malformed.
   */
  public ActionUnitExperimental(Class<?> klass) throws Throwable {
    super(klass);
    this.visitor = new Action.Visitor.Impl();
  }

  @Override
  protected void collectInitializationErrors(List<Throwable> errors) {
    ////
    // TODO Implement validations here.
  }

  @Override
  protected List<FrameworkMethod> getChildren() {
    return this.convertFrameworkMethods(super.getChildren());
  }

  @Override
  protected Description describeChild(FrameworkMethod method) {
    Description parentDescription = methodDescriptions.get(method.getMethod());

    if (parentDescription == null) {
      parentDescription = Description.createSuiteDescription(
          getTestClass().getName(),
          method.getMethod().getName()
      );
          //Description.createTestDescription(getTestClass().getJavaClass(),
          //testName(method), method.getAnnotations());
      methodDescriptions.putIfAbsent(method.getMethod(), parentDescription);
    }
    Description ret = Description.createTestDescription(getTestClass().getJavaClass(), method.getName(), method.getAnnotations());
    parentDescription.addChild(ret);
    return ret;
  }

  protected String testName(FrameworkMethod method) {
    return method.getMethod().getName();
  }

  private List<FrameworkMethod> convertFrameworkMethods(List<FrameworkMethod> testMethods) {
    final List<FrameworkMethod> ret = new LinkedList<>();
    for (final Map.Entry<FrameworkMethod, List<Action>> eachActionListEntry : Lists.transform(testMethods, new Function<FrameworkMethod, Map.Entry<FrameworkMethod, List<Action>>>() {
      @Override
      public Map.Entry<FrameworkMethod, List<Action>> apply(FrameworkMethod input) {
        return new AbstractMap.SimpleEntry<>(input, createActions(input));
      }
    })) {
      final int[] counter = new int[] { 0 };
      ret.addAll(Lists.transform(eachActionListEntry.getValue(), new Function<Action, FrameworkMethod>() {
        @Override
        public FrameworkMethod apply(Action input) {
          int i = eachActionListEntry.getValue().size() == 1
              ? -1
              : counter[0]++;
          return createFrameworkMethod(eachActionListEntry.getKey(), i, input);
        }
      }));
    }
    return ret;
  }

  private FrameworkMethod createFrameworkMethod(FrameworkMethod parent, final int index, final Action action) {
    return new FrameworkMethod(parent.getMethod()) {
      @Override
      public Object invokeExplosively(Object target, Object... args) {
        action.accept(ActionUnitExperimental.this.visitor);
        return null;
      }

      /**
       * Returns the method's name
       */
      @Override
      public String getName() {
        return index < 0
            ? super.getName()
            : format("%s[%d]", super.getName(), index);
      }

      @Override
      public boolean equals(Object obj) {
        return super.equals(obj)
            ? this.getName().equals(((FrameworkMethod)obj).getName())
            : false;
      }

      @Override
      public int hashCode() {
        return super.hashCode();
      }

    };
  }

  private List<Action> createActions(FrameworkMethod testMethod) {
    try {
      Object result = checkNotNull(testMethod.invokeExplosively(this.getTestClass().getJavaClass().newInstance()));
      if (result instanceof Action) {
        return Collections.singletonList((Action) result);
      }
      if (result.getClass().isArray() && Action.class.isAssignableFrom(result.getClass().getComponentType())) {
        return Arrays.asList((Action[]) result);
      }
      throw new RuntimeException(format("Unsupported type (%s)", result.getClass().getCanonicalName()));
    } catch (Throwable e) {
      throw Throwables.propagate(e);
    }
  }

}
