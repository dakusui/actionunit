package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.compat.utils.TestUtils;
import com.github.dakusui.actionunit.n.exceptions.ActionException;
import com.github.dakusui.actionunit.sandbox.AutocloseableIterator;
import org.junit.Test;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import static com.github.dakusui.actionunit.compat.utils.TestUtils.*;
import static com.sun.org.glassfish.gmbal.ManagedObjectManagerFactory.getMethod;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class UtilsTest {
  @Test
  public void whenRangeIntIsInvoked$thenWorksRight() {
    List<Integer> result = toList(
        range(1));
    assertEquals(
        singletonList(0),
        result
    );
  }

  @Test
  public void whenRangeIntIntIsInvoked$thenWorksRight() {
    List<Integer> result = toList(
        range(0, 3));

    assertEquals(
        asList(0, 1, 2),
        result
    );
  }

  @Test(expected = UnsupportedOperationException.class)
  public void givenRange$whenRemove$thenUnsupported() {
    range(0, 3).iterator().remove();
  }

  @Test
  public void whenRangeIntIntInIsInvoked$thenWorksRight() {
    List<Integer> result = toList(
        range(0, 3, 1));
    assertEquals(
        asList(0, 1, 2),
        result
    );
  }

  @Test
  public void givenStartAndStopAreAscendingAndNegativeStep$whenRangeIntIntInIsInvoked$thenEmptyReturned() {
    List<Integer> result = toList(
        range(0, 3, -1));
    assertEquals(
        emptyList(),
        result
    );
  }

  @Test
  public void givenStartAndStopAreDescendingAndNegativeStep$whenRangeIntIntInIsInvoked$thenWorksRight() {
    List<Integer> result = toList(
        range(3, 0, -1));
    assertEquals(
        asList(3, 2, 1),
        result
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void givenNegativeStep$whenRangeIsInvoked$thenIllegalArgument() {
    range(0, 1, 0);
  }


  @Test
  public void givenCollection$whenTransform$thenWorksCorrectly() {
    TestUtils.Out out = new TestUtils.Out();
    Collection<Integer> collection = (Collection<Integer>) TestUtils.transform(createAutoclosingCollection(out), String::length);
    try (AutocloseableIterator<Integer> i = (AutocloseableIterator<Integer>) collection.iterator()) {
      while (i.hasNext()) {
        out.writeLine(i.next().toString() + " characters");
      }
    }
    // Make sure 'iterator()' idempotent
    try (AutocloseableIterator<Integer> i = (AutocloseableIterator<Integer>) collection.iterator()) {
      while (i.hasNext()) {
        out.writeLine(i.next().toString() + " characters");
      }
    }
    assertThat(collection, instanceOf(Collection.class));
    //noinspection PointlessArithmeticExpression
    assertThat(out, allOf(
        hasItemAt(0, containsString("5 characters")),
        hasItemAt(1, containsString("6 characters")),
        hasItemAt(2, containsString("7 characters")),
        hasItemAt(3, containsString("closed@Collection")),
        hasItemAt(4 + 0, containsString("5 characters")),
        hasItemAt(4 + 1, containsString("6 characters")),
        hasItemAt(4 + 2, containsString("7 characters")),
        hasItemAt(4 + 3, containsString("closed@Collection"))
    ));
    assertEquals(8, out.size());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void givenTransformedCollection$whenCleared$thenUnsupportedException() {
    TestUtils.Out out = new TestUtils.Out();
    Collection<Integer> collection = (Collection<Integer>) TestUtils.transform(createAutoclosingCollection(out), new Function<String, Integer>() {
      @Override
      public Integer apply(String input) {
        return input.length();
      }
    });
    assertFalse(collection.isEmpty());
    collection.clear();
  }

  /**
   * Returns a collection whose {@code iterator()} method returns an iterator which
   * is also an {@code AutoCloseable}.
   */
  private Collection<String> createAutoclosingCollection(
      final TestUtils.Out out) {
    return new AbstractCollection<String>() {
      List<String> list = asList("Hello", "Hello1", "Hello12");

      @Override
      public Iterator<String> iterator() {
        class Ret implements Iterator<String>, AutoCloseable {
          Iterator<String> inner = list.iterator();

          @Override
          public void close() throws Exception {
            out.writeLine("closed@Collection");
          }

          @Override
          public boolean hasNext() {
            return inner.hasNext();
          }

          @Override
          public String next() {
            return inner.next();
          }

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }
        }
        return new Ret();
      }

      @Override
      public int size() {
        return list.size();
      }
    };
  }

  @Test
  public void givenNonCollectionAutoclosingIterable$whenTransform$thenWorksCorrectly
      () {
    final TestUtils.Out out = new TestUtils.Out();
    Iterable<Integer> iterable = TestUtils.transform(createAutoclosingIterable(out), input -> input.length());
    try (AutocloseableIterator<Integer> i = (AutocloseableIterator<Integer>) iterable.iterator()) {
      while (i.hasNext()) {
        out.writeLine(i.next().toString() + " characters");
      }
    }
    // Make sure 'iterator()' idempotent.
    try (AutocloseableIterator<Integer> i = (AutocloseableIterator<Integer>) iterable.iterator()) {
      while (i.hasNext()) {
        out.writeLine(i.next().toString() + " characters");
      }
    }
    assertThat(
        iterable, not(instanceOf(Collection.class))
    );
    //noinspection PointlessArithmeticExpression
    assertThat(out, allOf(
        hasItemAt(0, containsString("5 characters")),
        hasItemAt(1, containsString("6 characters")),
        hasItemAt(2, containsString("7 characters")),
        hasItemAt(3, containsString("closed@Iterable")),
        hasItemAt(4, containsString("closed@Collection")),
        hasItemAt(5 + 0, containsString("5 characters")),
        hasItemAt(5 + 1, containsString("6 characters")),
        hasItemAt(5 + 2, containsString("7 characters")),
        hasItemAt(5 + 3, containsString("closed@Iterable")),
        hasItemAt(5 + 4, containsString("closed@Collection"))
    ));
    assertEquals(10, out.size());
  }

  /**
   * Returns an iterable whose {@code iterator()} method returns an iterator which
   * is also an {@code AutoCloseable}.
   */
  private Iterable<String> createAutoclosingIterable(
      final TestUtils.Out out) {
    return () -> {
      class Ret implements Iterator<String>, AutoCloseable {
        Iterator<String> inner = createAutoclosingCollection(out).iterator();

        @Override
        public void close() throws Exception {
          out.writeLine("closed@Iterable");
          ((AutoCloseable) inner).close();
        }

        @Override
        public boolean hasNext() {
          return inner.hasNext();
        }

        @Override
        public String next() {
          return inner.next();
        }

        @Override
        public void remove() {
          throw new UnsupportedOperationException();
        }
      }
      return new Ret();
    };
  }

  @Test(expected = ActionException.class)
  public void givenNonExistingMethodName$whenGetMethodIsPerformed$thenWrappedExceptionThrown() {
    getMethod(Object.class, "notExistingMethod");
  }
}
