package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.actionunit.connectors.Source;
import com.google.common.base.Function;

import java.util.Iterator;

import static com.github.dakusui.actionunit.Utils.*;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.join;

/**
 * An action that is repeated on values given by an {@link Iterable&lt;T&gt;}.
 *
 * @param <T> A type of values on which this action is repeated.
 */
public class ForEach<T> extends ActionBase {
  private final Composite.Factory   factory;
  private final Iterable<Source<T>> dataSource;
  private final Action              action;
  private final Sink<T>[]           sinks;


  public ForEach(Composite.Factory factory, Iterable<Source<T>> dataSource, Action action, Sink<T>[] sinks) {
    this.factory = factory;
    this.dataSource = dataSource;
    this.action = checkNotNull(action);
    this.sinks = sinks;
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return format("%s (%s, %s items) {%s}",
        this.getClass().getSimpleName(),
        this.factory,
        unknownIfNegative(sizeOrNegativeIfNonCollection(this.dataSource)),
       join(
            transform(
                asList(sinks),
                new Function<Sink<T>, Object>() {
                  @Override
                  public Object apply(Sink<T> sink) {
                    return describe(sink);
                  }
                }
            ),
            ",")
    );
  }

  public Composite getElements() {
    final Function<Source<T>, Action> func = new Function<Source<T>, Action>() {
      @Override
      public Action apply(final Source<T> t) {
        //noinspection unchecked
        return createWithAction(t);
      }

      private With createWithAction(final Source<T> t) {
        return new MyWith<T>(t, ForEach.this.action, ForEach.this.sinks);
      }
    };
    return new MySequential((Sequential) ForEach.this.factory.create(transform(dataSource, func)));
  }

  private static class MySequential implements Sequential, IgnoredInPathCalculation {
    final Sequential sequential;

    MySequential(Sequential sequential) {
      this.sequential = sequential;
    }

    @Override
    public void accept(Visitor visitor) {
      visitor.visit(this);
    }

    @Override
    public int size() {
      return sequential.size();
    }

    @Override
    public Iterator<Action> iterator() {
      return sequential.iterator();
    }

    @Override
    public int hashCode() {
      return sequential.hashCode();
    }

    public boolean equals(Object object) {
      if (!(object instanceof MySequential)) {
        return false;
      }
      MySequential another = (MySequential) object;
      return this.sequential.equals(another.sequential);
    }
  }

  class MyWith<U> extends With.Base<U> implements Action.IgnoredInPathCalculation {
    public MyWith(Source<U> source, Action action, Sink<U>[] sinks) {
      super(source, action, sinks);
    }

    @Override
    public String toString() {
      return "With";
    }
  }

  public Action getAction() {
    return this.action;
  }

  public enum Mode {
    SEQUENTIALLY {
      @Override
      public Composite.Factory getFactory() {
        return Sequential.Factory.INSTANCE;
      }
    },
    CONCURRENTLY {
      @Override
      public Composite.Factory getFactory() {
        return Concurrent.Factory.INSTANCE;
      }
    };

    public abstract Composite.Factory getFactory();
  }
}
