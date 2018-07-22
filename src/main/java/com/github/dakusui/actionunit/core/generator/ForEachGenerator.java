package com.github.dakusui.actionunit.core.generator;

import com.github.dakusui.actionunit.actions.ForEach;
import com.github.dakusui.actionunit.actions.ValueHolder;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;

import java.util.function.Function;
import java.util.function.Supplier;

public interface ForEachGenerator<I> extends ActionGenerator<I> {
  default ForEachGenerator<I> sequentially() {
    bean().sequentially();
    return this;
  }

  default ForEachGenerator<I> concurrently() {
    bean().concurrently();
    return this;
  }

  Bean<I> bean();

  static <I, E> ForEachGenerator<E> create(StreamGenerator<I, E> data) {
    return new ForEachGenerator<E>() {
      @Override
      public Function<Context, Action> apply(ValueHolder<E> valueHolder) {
        return null;
      }

      final Bean<E> bean = new Bean<>(data);

      @Override
      public Bean<E> bean() {
        return bean;
      }

    };
  }

  class Bean<E> {
    private final StreamGenerator data;
    private       ForEach.Mode       mode = ForEach.Mode.SEQUENTIALLY;

    public Bean(StreamGenerator data) {
      this.data = data;
    }

    void sequentially() {
      this.mode = ForEach.Mode.SEQUENTIALLY;
    }

    public void concurrently() {
      this.mode = ForEach.Mode.CONCURRENTLY;
    }
  }
}
