package com.github.dakusui.actionunit.helpers;

import com.github.dakusui.actionunit.exceptions.ActionException;
import org.junit.runners.Parameterized;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static com.github.dakusui.actionunit.exceptions.ActionException.wrap;
import static com.github.dakusui.actionunit.helpers.Checks.checkArgument;
import static com.github.dakusui.actionunit.helpers.Checks.checkNotNull;
import static java.lang.String.format;
import static java.util.Collections.singletonList;

public class InternalUtils {
  public static <T> T runWithTimeout(Callable<T> callable, long timeout, TimeUnit timeUnit) {
    final ExecutorService executor = Executors.newSingleThreadExecutor();
    final Future<T> future = executor.submit(callable);
    executor.shutdown(); // This does not cancel the already-scheduled task.
    try {
      return future.get(timeout, timeUnit);
    } catch (InterruptedException e) {
      throw ActionException.wrap(e);
    } catch (TimeoutException e) {
      future.cancel(true);
      throw ActionException.wrap(e);
    } catch (ExecutionException e) {
      //unwrap the root cause
      Throwable cause = e.getCause();
      if (cause instanceof Error) {
        throw (Error) cause;
      }
      ////
      // It's safe to directly cast to RuntimeException, because a Callable can only
      // throw an Error or a RuntimeException.
      throw (RuntimeException) cause;
    } finally {
      executor.shutdownNow();
    }
  }

  private static TimeUnit chooseTimeUnit(long intervalInNanos) {
    // TimeUnit.values() returns elements of TimeUnit in declared order
    // And they are declared in ascending order.
    for (TimeUnit timeUnit : TimeUnit.values()) {
      if (1000 > timeUnit.convert(intervalInNanos, TimeUnit.NANOSECONDS)) {
        return timeUnit;
      }
    }
    return TimeUnit.DAYS;
  }

  public static String formatDuration(long durationInNanos) {
    TimeUnit timeUnit = chooseTimeUnit(durationInNanos);
    return format("%d[%s]", timeUnit.convert(durationInNanos, TimeUnit.NANOSECONDS), timeUnit.toString().toLowerCase());
  }

  public static String shortClassNameOf(Class clazz) {
    String name = checkNotNull(clazz).getName();
    return name.substring(name.lastIndexOf('.') + 1);
  }

  public static String nonameIfNull(String summary) {
    return summary == null
        ? "(noname)"
        : summary;
  }

  public static String unknownIfNegative(int size) {
    return size < 0
        ? "?"
        : Integer.toString(size);
  }

  public static boolean isGivenTypeExpected_ArrayOfExpected_OrIterable(Class<?> expected, Class<?> actual) {
    return expected.isAssignableFrom(actual)
        || (actual.isArray() && expected.isAssignableFrom(actual.getComponentType()))
        || Iterable.class.isAssignableFrom(actual);
  }

  /**
   * Creates a {@code TestClass} object to mock {@code Parameterized} class's logic
   * which cannot be overridden.
   * Some IDE(s) (IntelliJ, for instance) treat classes that extend {@code Parameterized}
   * runner in a special way. And therefore we have to extend it to allow users to
   * use IDE features for parameterized runners.
   *
   * @param testClass original test class object.
   * @return Created {@code TestClass} object.
   */
  public static TestClass createTestClassMock(final TestClass testClass) {
    return new TestClass(testClass.getJavaClass()) {
      @Override
      public List<FrameworkMethod> getAnnotatedMethods(final Class<? extends Annotation> annClass) {
        if (Parameterized.Parameters.class.equals(annClass)) {
          return singletonList(createDummyFrameworkMethod());
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
            checkArgument(Parameterized.Parameters.class.equals(annotationType));
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
        return getToStringMethod(Object.class);
      }
    };
  }

  /**
   * Tries to describe given {@code obj} in a best possible way.
   *
   * @param obj An object to be described.
   * @return A description of {@code obj}.
   */
  public static String describe(Object obj) {
    if (obj == null) {
      return "null";
    }
    if (getToStringMethod(obj.getClass()).equals(getToStringMethod(Object.class))) {
      return describeClassOf(obj);
    }
    return obj.toString();
  }

  public static String summary(String s) {
    return Objects.requireNonNull(s).length() > 40
        ? s.substring(0, 40) + "..."
        : s;
  }

  private static String describeClassOf(Object obj) {
    return shortClassNameOf(checkNotNull(obj).getClass());
  }

  public static void sleep(long duration, TimeUnit timeUnit) {
    try {
      checkNotNull(timeUnit).sleep(duration);
    } catch (InterruptedException e) {
      throw ActionException.wrap(e);
    }
  }

  /**
   * Returns a method without parameters which has a given {@code methodName} from
   * a Class {@code klass}.
   *
   * @param klass      A class from which method is searched.
   * @param methodName A name of method to be returned.
   * @return A method object of the specified name in the {@code klass}.
   */
  public static Method getMethod(Class<?> klass, String methodName) {
    try {
      ////
      // Just chose "toString" because we know java.lang.Object has the method.
      return checkNotNull(klass).getMethod(checkNotNull(methodName));
    } catch (NoSuchMethodException e) {
      throw wrap(e);
    }
  }

  public static <T> List<T> toList(Iterable<T> iterable) {
    return new LinkedList<T>() {{
      for (T each : iterable)
        add(each);
    }};
  }

  private static Method getToStringMethod(Class<?> klass) {
    return getMethod(klass, "toString");
  }

  public static boolean elementsEqual(Iterable<?> left, Iterable<?> right) {
    Iterator iLeft = checkNotNull(left.iterator());
    Iterator iRight = checkNotNull(right.iterator());
    while (true) {
      if (iLeft.hasNext()) {
        if (!iRight.hasNext())
          return false;
        if (Objects.equals(iLeft.next(), iRight.next()))
          continue;
        return false;
      }
      return !iRight.hasNext();
    }
  }

  public static <T, E extends RuntimeException> Collector<T, List<T>, Optional<T>> singletonCollector(Supplier<E> exceptionSupplier) throws E {
    /*
     * Borrowed from the following place.
     * https://stackoverflow.com/questions/22694884/filter-java-stream-to-1-and-only-1-element
     */
    return Collector.of(
        ArrayList::new,
        List::add,
        (left, right) -> {
          left.addAll(right);
          return left;
        },
        list -> {
          if (list.size() > 1)
            throw exceptionSupplier.get();
          return list.isEmpty()
              ? Optional.empty()
              : Optional.of(list.get(0));
        }
    );
  }

  public static <T> Supplier<T> describable(String description, T value) {
    return new Supplier<T>() {
      @Override
      public T get() {
        return value;
      }

      @Override
      public String toString() {
        return description;
      }
    };
  }
}
