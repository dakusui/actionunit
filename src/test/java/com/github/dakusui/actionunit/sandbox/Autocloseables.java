package com.github.dakusui.actionunit.sandbox;

import com.github.dakusui.actionunit.exceptions.ActionException;

import java.util.Iterator;

public enum Autocloseables {
  ;

  public static <T> AutocloseableIterator<T> autocloseable(final Iterator<T> iterator) {
    if (iterator instanceof AutocloseableIterator) {
      return (AutocloseableIterator<T>) iterator;
    }
    return autocloseable(
        iterator,
        toAutocloseable(iterator)
    );
  }

  public static <T> AutocloseableIterator<T> autocloseable(final Iterator<T> iterator, final AutoCloseable resource) {
    return new AutocloseableIterator<T>() {
      @Override
      public void close() {
        if (resource != null) {
          try {
            resource.close();
          } catch (Exception e) {
            throw ActionException.wrap(e);
          }
        }
      }

      @Override
      public boolean hasNext() {
        return iterator.hasNext();
      }

      @Override
      public T next() {
        return iterator.next();
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  /**
   * Casts given object to {@code AutoCloseable} if possible. Or {@code null}.
   */
  public static AutoCloseable toAutocloseable(Object i) {
    return i instanceof AutoCloseable
        ? (AutoCloseable) i
        : null;
  }
}
