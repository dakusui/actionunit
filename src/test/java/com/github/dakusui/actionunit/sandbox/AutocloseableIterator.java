package com.github.dakusui.actionunit.sandbox;

import com.github.dakusui.actionunit.compat.utils.TestUtils;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;

import static java.util.Arrays.asList;

public interface AutocloseableIterator<E> extends AutoCloseable, Iterator<E> {
  @SafeVarargs
  static <T> List<T> autocloseableList(final TestUtils.Out out, final String msg, final T... values) {
    return new AbstractList<T>() {
      public Iterator<T> iterator() {
        class I implements Iterator<T>, AutoCloseable {
          final Iterator<T> inner = asList(values).iterator();

          @Override
          public void close() throws Exception {
            out.writeLine(msg);
          }

          @Override
          public boolean hasNext() {
            return inner.hasNext();
          }

          @Override
          public T next() {
            return inner.next();
          }

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }
        }
        return new I();
      }

      @Override
      public T get(int index) {
        return values[index];
      }

      @Override
      public int size() {
        return values.length;
      }
    };
  }

  @Override
  void close();

  interface Factory<E> extends Iterable<E> {
    @Override
    AutocloseableIterator<E> iterator();
  }
}
