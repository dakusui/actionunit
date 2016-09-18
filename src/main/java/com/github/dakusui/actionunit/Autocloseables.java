package com.github.dakusui.actionunit;

import com.github.dakusui.actionunit.exceptions.ActionException;
import com.google.common.base.Function;
import com.google.common.collect.Iterators;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

public enum Autocloseables {
  ;

  public static <I, O> Iterable<O> transform(final Iterable<I> in, final Function<? super I, ? extends O> function) {
    return new AutocloseableIterator.Factory<O>() {
      @Override
      public AutocloseableIterator<O> iterator() {
        Iterator<I> i = in.iterator();
        Iterator<O> o = Iterators.transform(i, function);
        return autocloseable(
            o,
            toAutocloseable(i)
        );
      }
    };
  }

  public static <I, O> Collection<O> transform(final Collection<I> in, final Function<? super I, O> function) {
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
        return autocloseable(
            Iterators.transform(i, function),
            toAutocloseable(i));
      }

      @Override
      public int size() {
        return in.size();
      }
    };
  }

  public static <T> AutocloseableIterator<T> autocloseable(final Iterator<T> iterator) {
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
