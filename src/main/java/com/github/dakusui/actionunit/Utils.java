package com.github.dakusui.actionunit;

import com.github.dakusui.actionunit.exceptions.ActionException;
import org.junit.runners.Parameterized;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.github.dakusui.actionunit.Checks.checkArgument;
import static com.github.dakusui.actionunit.Checks.checkNotNull;
import static com.github.dakusui.actionunit.exceptions.ActionException.wrap;
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

  public static boolean isGivenTypeExpected_ArrayOfExpected_OrIterable(Class<?> expected, Class<?> actual) {
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
    if (getToStringMethod(obj.getClass()).equals(getToStringMethod(Object.class))) {
      return describeClassOf(obj);
    }
    return obj.toString();
  }

  public static String describeClassOf(Object obj) {
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

  public static <T> T[] toArray(Iterable<T> iterable, Class<T> klass) {
    //noinspection unchecked
    return toList(iterable).toArray((T[]) Array.newInstance(klass, 0));
  }

  public static <T, U> Iterator<U> transform(Iterator<T> iterator, Function<? super T, ? extends U> func) {
    checkNotNull(iterator);
    checkNotNull(func);
    return new Iterator<U>() {

      @Override
      public boolean hasNext() {
        return iterator.hasNext();
      }

      @Override
      public U next() {
        return func.apply(iterator.next());
      }
    };
  }

  public static <T> Stream<T> toStream(Iterable<T> iterable) {
    return StreamSupport.stream(iterable.spliterator(), false);
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

  public static <T> int size(Iterable<? super T> iterable) {
    if (iterable instanceof Collection)
      return ((Collection) iterable).size();
    return toList(iterable).size();
  }
}
