package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.Actions;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public interface ForEach<T> extends Action {
  Iterable<T> data();

  Action createProcessor(Supplier<T> data);

  Composite.Factory getCompositeFactory();

  static <E> ForEach.Builder<E> builder(Iterable<? extends E> elements) {
    return new ForEach.Builder<>(elements);
  }

  class Builder<E> {
    private final Iterable<? extends E> elements;
    private Mode mode = Mode.SEQUENTIALLY;

    Builder(Iterable<? extends E> elements) {
      this.elements = Objects.requireNonNull(elements);
    }

    public Builder<E> sequentially() {
      this.mode = Mode.SEQUENTIALLY;
      return this;
    }

    public Builder<E> concurrently() {
      this.mode = Mode.CONCURRENTLY;
      return this;
    }

    public ForEach<E> perform(HandlerFactory<E> operation) {
      Objects.requireNonNull(operation);
      Objects.requireNonNull(operation);
      //noinspection unchecked
      return new ForEach.Impl<>(
          operation,
          (Iterable<E>) this.elements,
          this.mode.getFactory()
      );
    }
  }

  enum Mode {
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

  class Impl<T> extends ActionBase implements ForEach<T> {
    private final Function<Supplier<T>, Action> processorFactory;
    private final Iterable<T>                   data;
    private final Composite.Factory             compositeFactory;

    public Impl(Function<Supplier<T>, Action> handlerFactory, Iterable<T> data, Composite.Factory compositeFactory) {
      this.processorFactory = Objects.requireNonNull(handlerFactory);
      this.data = Objects.requireNonNull(data);
      this.compositeFactory = Objects.requireNonNull(compositeFactory);
    }

    @Override
    public Iterable<T> data() {
      return this.data;
    }

    @Override
    public Action createProcessor(Supplier<T> data) {
      return Actions.named(String.format("ForEach:%s", this.data), this.processorFactory.apply(data));
    }

    @Override
    public Composite.Factory getCompositeFactory() {
      return this.compositeFactory;
    }

    @Override
    public void accept(Visitor visitor) {
      visitor.visit(this);
    }
  }
}
