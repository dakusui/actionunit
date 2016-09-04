package com.github.dakusui.actionunit;

import com.github.dakusui.actionunit.exceptions.ActionException;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import org.junit.runners.Parameterized;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

import static com.github.dakusui.actionunit.exceptions.ActionException.wrap;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static java.util.Collections.singletonList;

/**
 * A utility class for static methods which are too trivial to create classes to which they should
 * belong.
 */
public enum Utils {
  ;

  public static <T> T runWithTimeout(Callable<T> callable, long timeout, TimeUnit timeUnit) {
    final ExecutorService executor = Executors.newSingleThreadExecutor();
    final Future<T> future = executor.submit(callable);
    executor.shutdown(); // This does not cancel the already-scheduled task.
    try {
      return future.get(timeout, timeUnit);
    } catch (InterruptedException e) {
      throw new ActionException(e);
    } catch (TimeoutException e) {
      future.cancel(true);
      throw new ActionException(e);
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

  public static <T> int sizeOrNegativeIfNonCollection(Iterable<T> iterable) {
    checkNotNull(iterable);
    if (iterable instanceof Collection) {
      return Collection.class.cast(iterable).size();
    }
    return -1;
  }

  static boolean isGivenTypeExpected_ArrayOfExpected_OrIterable(Class<?> expected, Class<?> actual) {
    return expected.isAssignableFrom(actual)
        || (actual.isArray() && expected.isAssignableFrom(actual.getComponentType()))
        || Iterable.class.isAssignableFrom(actual);
  }

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
          ////
          // Just chose "toString" because we know java.lang.Object has the method.
          return Object.class.getMethod("toString");
        } catch (NoSuchMethodException e) {
          throw wrap(e);
        }
      }
    };
  }

  public static <I, O> Iterable<O> transform(Iterable<I> in, Function<? super I, ? extends O> func) {
    checkNotNull(func);
    if (in instanceof Collection) {
      //noinspection unchecked
      return (Iterable<O>) Collections2.transform((Collection<I>) in, func);
    }
    return Iterables.transform(in, func);
  }

  /**
   * Returns an iterable object that covers specified range by arguments given
   * to this method.
   *
   * @param start A number from which returned iterable object starts
   * @param stop  A number at which returned iterable object stops
   * @param step  A number by which returned iterable object increases. positive
   *              and negative are allowed, but zero is not.
   */
  public static Iterable<Integer> range(final int start, final int stop, final int step) {
    checkArgument(step != 0, "step argument must not be zero. ");
    return new Iterable<Integer>() {
      @Override
      public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {
          long current = start;

          @Override
          public boolean hasNext() {
            long next = current + step;
            // If next value goes over int range, returned iterator will stop.
            //noinspection SimplifiableIfStatement
            if (next > Integer.MAX_VALUE || next < Integer.MIN_VALUE)
              return false;
            return Math.signum(step) > 0
                ? next <= stop
                : next >= stop;
          }

          @Override
          public Integer next() {
            try {
              return (int) current;
            } finally {
              current += step;
            }
          }

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }

  /**
   * Equivalent to {@code range(start, stop, 1)}.
   *
   * @see Utils#range(int, int, int)
   */
  public static Iterable<Integer> range(int start, int stop) {
    return range(start, stop, 1);
  }

  /**
   * Equivalent to {@code range(0, stop)}.
   *
   * @see Utils#range(int, int)
   */
  public static Iterable<Integer> range(int stop) {
    return range(0, stop);
  }

  /**
   * Tries to describe given {@code obj} in a best possible way.
   *
   * @param obj An object to be described.
   */
  public static String describe(Object obj) {
    if (obj == null) {
      return "null";
    }
    try {
      if (obj.getClass().getMethod("toString").equals(Object.class.getMethod("toString"))) {
        return shortClassNameOf(obj.getClass());
      }
    } catch (NoSuchMethodException e) {
      throw wrap(e);
    }
    return obj.toString();
  }

  public static String describeClassOf(Object obj) {
    return shortClassNameOf(checkNotNull(obj).getClass());
  }
}
