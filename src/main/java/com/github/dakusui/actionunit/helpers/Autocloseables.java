package com.github.dakusui.actionunit.helpers;

import com.github.dakusui.actionunit.core.AutocloseableIterator;
import com.github.dakusui.actionunit.exceptions.ActionException;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;

import static com.github.dakusui.actionunit.helpers.Checks.checkNotNull;

public enum Autocloseables {
  ;

  public static <I, O> AutocloseableIterator<O> transform(final AutocloseableIterator<I> in, final Function<? super I, ? extends O> function) {
    return new AutocloseableIterator<O>() {
      @Override
      public void close() {
        in.close();
      }

      @Override
      public boolean hasNext() {
        return in.hasNext();
      }

      @Override
      public O next() {
        return function.apply(in.next());
      }

      @Override
      public void remove() {
        in.remove();
      }
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
      Iterator<O> o = Utils.transform(i, func);
      return autocloseable(
          o,
          toAutocloseable(i)
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
        return autocloseable(
            Utils.transform(i, func),
            toAutocloseable(i));
      }

      @Override
      public int size() {
        return in.size();
      }
    };
  }

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
