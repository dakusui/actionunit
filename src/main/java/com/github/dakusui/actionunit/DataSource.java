package com.github.dakusui.actionunit;

import com.github.dakusui.actionunit.compat.Context;

import java.util.function.Function;

import static com.github.dakusui.actionunit.Checks.checkNotNull;
import static com.github.dakusui.actionunit.Utils.sizeOrNegativeIfNonCollection;

public interface DataSource<T> extends Iterable<T> {
  @Override
  AutocloseableIterator<T> iterator();

  interface Factory<T> {
    DataSource<T> create(Context context);

    /**
     * Returns number of elements to be iterated by iterator returned by {@code create()} method.
     * If size is not known at the time this method is called, implementation may return {@code -1}.
     *
     * @see Factory#create
     */
    int size();

    abstract class Base<T> implements DataSource.Factory<T> {

      @Override
      final public DataSource<T> create(Context context) {
        final Iterable<T> iterable = checkNotNull(iterable(context));
        return new DataSource<T>() {
          @Override
          public AutocloseableIterator<T> iterator() {
            return Autocloseables.autocloseable(iterable.iterator());
          }
        };
      }

      abstract protected Iterable<T> iterable(Context context);

      /**
       * {@inheritDoc}
       * <p>
       * Override this method if the number of elements returned by {@code iterable}
       * is known beforehand.
       */
      @Override
      public int size() {
        return -1;
      }
    }

    class PassThrough<T> extends Base<T> implements Factory<T> {
      private final Iterable<T> dataSource;

      public PassThrough(final Iterable<T> dataSource) {
        this.dataSource = checkNotNull(dataSource);
      }

      @Override
      protected Iterable<T> iterable(Context context) {
        return this.dataSource;
      }

      @Override
      public int size() {
        return sizeOrNegativeIfNonCollection(this.dataSource);
      }
    }

    class Adapter<T, U> implements Factory<U> {
      private final Function<T, U> translator;
      private final Factory<T>     base;

      public Adapter(DataSource.Factory<T> base, Function<T, U> translator) {
        this.translator = checkNotNull(translator);
        this.base = base;
      }

      @Override
      public DataSource<U> create(final Context context) {
        return new DataSource<U>() {
          @Override
          public AutocloseableIterator<U> iterator() {
            return Autocloseables.transform(base.create(context).iterator(), translator);
          }
        };
      }

      @Override
      public int size() {
        return base.size();
      }
    }
  }
}