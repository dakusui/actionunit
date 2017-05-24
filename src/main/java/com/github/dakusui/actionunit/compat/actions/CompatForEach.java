package com.github.dakusui.actionunit.compat.actions;

import com.github.dakusui.actionunit.actions.Composite;
import com.github.dakusui.actionunit.actions.Nested;
import com.github.dakusui.actionunit.compat.Context;
import com.github.dakusui.actionunit.core.DataSource;
import com.github.dakusui.actionunit.compat.connectors.Sink;
import com.github.dakusui.actionunit.compat.connectors.Source;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.helpers.Autocloseables;
import com.github.dakusui.actionunit.helpers.Utils;
import com.github.dakusui.actionunit.visitors.ActionRunner;

import java.util.function.Function;

import static com.github.dakusui.actionunit.helpers.Checks.checkNotNull;
import static com.github.dakusui.actionunit.helpers.Utils.unknownIfNegative;
import static java.lang.String.format;
import static java.util.Arrays.asList;

/**
 * An action that is repeated on values given by an {@link Iterable&lt;T&gt;}.
 *
 * @param <T> A type of values on which this action is repeated.
 */
public class CompatForEach<T> extends Nested.Base {
  private final Composite.Factory             factory;
  private final DataSource.Factory<Source<T>> dataSourceFactory;
  private final Sink<T>[]                     sinks;

  public CompatForEach(Composite.Factory factory, DataSource.Factory<Source<T>> dataSourceFactory, Action action, Sink<T>[] sinks) {
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
        String.join(
            ",",
            Autocloseables.transform(
                asList(sinks),
                Utils::describe
            )));
  }

  public Composite getElements(Context context) {
    final Function<Source<T>, Action> func = new Function<Source<T>, Action>() {
      @Override
      public Action apply(final Source<T> t) {
        //noinspection unchecked
        return createWithAction(t);
      }

      private ActionRunner.IgnoredInPathCalculation.With createWithAction(final Source<T> t) {
        return new ActionRunner.WithResult.IgnoredInPathCalculation.With<T>(t, CompatForEach.this.getAction(), CompatForEach.this.sinks);
      }
    };
    return ActionRunner.IgnoredInPathCalculation.Composite.create(CompatForEach.this.factory.create(Autocloseables.transform(dataSourceFactory.create(context), func)));
  }

}
