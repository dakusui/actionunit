package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.*;
import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.actionunit.connectors.Source;
import com.github.dakusui.actionunit.visitors.ActionRunner;

import java.util.function.Function;

import static com.github.dakusui.actionunit.Checks.checkNotNull;
import static com.github.dakusui.actionunit.Utils.unknownIfNegative;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.join;

/**
 * An action that is repeated on values given by an {@link Iterable&lt;T&gt;}.
 *
 * @param <T> A type of values on which this action is repeated.
 */
public class ForEach<T> extends Nested.Base {
  private final Composite.Factory             factory;
  private final DataSource.Factory<Source<T>> dataSourceFactory;
  private final Sink<T>[]                     sinks;

  public ForEach(Composite.Factory factory, DataSource.Factory<Source<T>> dataSourceFactory, Action action, Sink<T>[] sinks) {
    super(action);
    this.factory = factory;
    this.dataSourceFactory = checkNotNull(dataSourceFactory);
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
        unknownIfNegative(this.dataSourceFactory.size()),
        join(
            Autocloseables.transform(
                asList(sinks),
                Utils::describe
            ),
            ","));
  }

  public Composite getElements(Context context) {
    final Function<Source<T>, Action> func = new Function<Source<T>, Action>() {
      @Override
      public Action apply(final Source<T> t) {
        //noinspection unchecked
        return createWithAction(t);
      }

      private With createWithAction(final Source<T> t) {
        return new ActionRunner.WithResult.IgnoredInPathCalculation.With<>(t, ForEach.this.getAction(), ForEach.this.sinks);
      }
    };
    return ActionRunner.IgnoredInPathCalculation.Composite.create(ForEach.this.factory.create(Autocloseables.transform(dataSourceFactory.create(context), func)));
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
