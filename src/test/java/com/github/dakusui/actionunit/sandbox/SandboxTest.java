package com.github.dakusui.actionunit.sandbox;

import com.github.dakusui.actionunit.ut.utils.TestUtils;
import com.github.dakusui.crest.Crest;
import org.junit.Test;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import static com.github.dakusui.actionunit.utils.Checks.checkNotNull;
import static com.github.dakusui.crest.Crest.asInteger;
import static com.github.dakusui.crest.Crest.asString;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public class SandboxTest {
  /**
   * Returns a collection whose {@code iterator()} method returns an iterator which
   * is also an {@code AutoCloseable}.
   */
  public static Collection<String> createAutoclosingCollection(
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

  /**
   * Returns an iterable whose {@code iterator()} method returns an iterator which
   * is also an {@code AutoCloseable}.
   */
  public static Iterable<String> createAutoclosingIterable(
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

  public static <I, O> Iterable<O> transform(final Iterable<I> in, final Function<? super I, ? extends O> func) {
    if (in instanceof Collection) {
      //noinspection unchecked,RedundantCast
      return (Collection<O>) transformCollection((Collection<I>) in, (Function<? super I, O>) func);
    }
    return transformIterable(in, func);
  }

  public static <I, O> Iterable<O> transformIterable(final Iterable<I> in, final Function<? super I, ? extends O> func) {
    checkNotNull(func);
    return (AutocloseableIterator.Factory<O>) () -> {
      Iterator<I> i = in.iterator();
      Iterator<O> o = TestUtils.transform(i, func);
      return Autocloseables.autocloseable(
          o,
          Autocloseables.toAutocloseable(i)
      );
    };
  }

  public static <I, O> Collection<O> transformCollection(final Collection<I> in, final Function<? super I, O> func) {
    checkNotNull(func);
    return new AbstractCollection<O>() {
      @Override
      public void clear() {
        in.clear();
      }

      @Override
      public boolean isEmpty() {
        return in.isEmpty();
      }

      @Override
      public Iterator<O> iterator() {
        Iterator<I> i = in.iterator();
        return Autocloseables.autocloseable(
            TestUtils.transform(i, func),
            Autocloseables.toAutocloseable(i));
      }

      @Override
      public int size() {
        return in.size();
      }
    };
  }

  @Test
  public void givenCollection$whenTransform$thenWorksCorrectly() {
    TestUtils.Out out = new TestUtils.Out();
    Collection<Integer> collection = (Collection<Integer>) transform(createAutoclosingCollection(out), String::length);
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
    Crest.assertThat(out, Crest.allOf(
        asString("get", 0).containsString("5 characters").$(),
        asString("get", 1).containsString("6 characters").$(),
        asString("get", 2).containsString("7 characters").$(),
        asString("get", 3).containsString("closed@Collection").$(),
        asString("get", 4 + 0).containsString("5 characters").$(),
        asString("get", 4 + 1).containsString("6 characters").$(),
        asString("get", 4 + 2).containsString("7 characters").$(),
        asString("get", 4 + 3).containsString("closed@Collection").$(),
        asInteger("size").equalTo(8).$()
    ));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void givenTransformedCollection$whenCleared$thenUnsupportedException() {
    TestUtils.Out out = new TestUtils.Out();
    Collection<Integer> collection = (Collection<Integer>) SandboxTest.transform(SandboxTest.createAutoclosingCollection(out), new Function<String, Integer>() {
      @Override
      public Integer apply(String input) {
        return input.length();
      }
    });
    assertFalse(collection.isEmpty());
    collection.clear();
  }

  @Test
  public void givenNonCollectionAutoclosingIterable$whenTransform$thenWorksCorrectly
      () {
    final TestUtils.Out out = new TestUtils.Out();
    Iterable<Integer> iterable = SandboxTest.transform(SandboxTest.createAutoclosingIterable(out), input -> input.length());
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
    Crest.assertThat(out, Crest.allOf(
        asString("get", 0).containsString("5 characters").$(),
        asString("get", 1).containsString("6 characters").$(),
        asString("get", 2).containsString("7 characters").$(),
        asString("get", 3).containsString("closed@Iterable").$(),
        asString("get", 4).containsString("closed@Collection").$(),
        asString("get", 5 + 0).containsString("5 characters").$(),
        asString("get", 5 + 1).containsString("6 characters").$(),
        asString("get", 5 + 2).containsString("7 characters").$(),
        asString("get", 5 + 3).containsString("closed@Iterable").$(),
        asString("get", 5 + 4).containsString("closed@Collection").$(),
        asInteger("size").equalTo(10).$()
    ));
  }

}
