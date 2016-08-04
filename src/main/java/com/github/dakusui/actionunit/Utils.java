package com.github.dakusui.actionunit;

import com.google.common.base.Preconditions;
import org.junit.runners.Parameterized;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import static com.google.common.base.Throwables.propagate;

public enum Utils {
  ;

  /**
   * Creates a {@code TestClass} object to mock {@code Parameterized} class's logic
   * which cannot be overridden.
   * <p/>
   * Some IDE(s) (IntelliJ, for instance) treat classes that extend {@code Parameterized}
   * runner in a special way. And therefore we have to extend it to allow users to
   * use IDE features for parameterized runners.
   *
   * @param testClass original test class object.
   */
  static TestClass createTestClassMock(final TestClass testClass) {
    return new TestClass(testClass.getJavaClass()) {
      @Override
      public List<FrameworkMethod> getAnnotatedMethods(final Class<? extends Annotation> annClass) {
        if (Parameterized.Parameters.class.equals(annClass)) {
          return Collections.singletonList(createDummyFrameworkMethod());
        }
        return super.getAnnotatedMethods(annClass);
      }

      private FrameworkMethod createDummyFrameworkMethod() {
        return new FrameworkMethod(getDummyMethod()) {
          public boolean isStatic() {
            return true;
          }

          @Override
          public Object invokeExplosively(Object target, Object... params) {
            return new Object[] {};
          }

          @Override
          public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
            Preconditions.checkArgument(Parameterized.Parameters.class.equals(annotationType));
            //noinspection unchecked
            return (T) new Parameterized.Parameters() {
              @Override
              public Class<? extends Annotation> annotationType() {
                return Parameterized.Parameters.class;
              }

              @Override
              public String name() {
                return "{index}";
              }
            };
          }
        };
      }

      private Method getDummyMethod() {
        try {
          return Object.class.getMethod("toString");
        } catch (NoSuchMethodException e) {
          throw propagate(e);
        }
      }
    };
  }

  public static <T> T runWithTimeout(Callable<T> callable, long timeout, TimeUnit timeUnit) {
    final ExecutorService executor = Executors.newSingleThreadExecutor();
    final Future<T> future = executor.submit(callable);
    executor.shutdown(); // This does not cancel the already-scheduled task.
    try {
      return future.get(timeout, timeUnit);
    } catch (InterruptedException e) {
      throw propagate(e);
    } catch (TimeoutException e) {
      future.cancel(true);
      throw new ActionException(e);
    } catch (ExecutionException e) {
      //unwrap the root cause
      Throwable t = e.getCause();
      if (t instanceof Error) {
        throw (Error) t;
      } else if (t instanceof RuntimeException) {
        throw (RuntimeException) t;
      } else {
        throw propagate(t);
      }
    }
  }

  public static TimeUnit chooseTimeUnit(long intervalInNanos) {
    // TimeUnit.values() returns elements of TimeUnit in declared order
    // And they are declared in ascending order.
    for (TimeUnit timeUnit : TimeUnit.values()) {
      if (1000 > timeUnit.convert(intervalInNanos, TimeUnit.NANOSECONDS)) {
        return timeUnit;
      }
    }
    return TimeUnit.DAYS;
  }

  static boolean isGivenTypeExpected_ArrayOfExpected_OrIterable(Class<?> expected, Class<?> actual) {
    return expected.isAssignableFrom(actual)
        || (actual.isArray() && expected.isAssignableFrom(actual.getComponentType()))
        || Iterable.class.isAssignableFrom(actual);
  }
}
