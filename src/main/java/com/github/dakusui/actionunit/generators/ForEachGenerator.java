package com.github.dakusui.actionunit.generators;

import com.github.dakusui.actionunit.actions.ValueHolder;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface ForEachGenerator<I, E> extends ActionGenerator<I> {
  ForEachGenerator<I, E> perform(ActionGenerator<E> handler);

  static <I, E> ForEachGenerator<I, E> create(StreamGenerator<I, E> data) {
    return new Impl<>(data);
  }

  static <I, E> ForEachGenerator<I, E> create(Supplier<Stream<E>> data) {
    return new Impl<>(v -> c -> data.get());
  }

  class Impl<I, E> implements ForEachGenerator<I, E> {
    final StreamGenerator<I, E> data;
    ActionGenerator<E> handler = NopGenerator.instance();

    public Impl(StreamGenerator<I, E> data) {
      this.data = data;
    }

    @Override
    public ForEachGenerator<I, E> perform(ActionGenerator<E> handler) {
      this.handler = handler;
      return this;
    }

    @Override
    public Function<Context, Action> apply(ValueHolder<I> valueHolder) {
      return context -> context.forEachOf(
          () -> data.apply(valueHolder, context)
      ).perform(handler);
    }
  }
}
