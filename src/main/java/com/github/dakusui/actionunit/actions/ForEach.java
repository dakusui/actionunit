package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public interface ForEach<T> extends Action {
  Iterable<T> data();

  Action createHandler(Supplier<T> data);

  Mode getMode();

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
          this.mode
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
    private final Function<Supplier<T>, Action> handlerFactory;
    private final Iterable<T>                   data;
    private final Mode                          mode;

    public Impl(Function<Supplier<T>, Action> handlerFactory, Iterable<T> data, Mode mode) {
      this.handlerFactory = Objects.requireNonNull(handlerFactory);
      this.data = Objects.requireNonNull(data);
      this.mode = Objects.requireNonNull(mode);
    }

    @Override
    public Iterable<T> data() {
      return this.data;
    }

    @Override
    public Action createHandler(Supplier<T> data) {
      return this.handlerFactory.apply(data);
    }

    @Override
    public Mode getMode() {
      return this.mode;
    }

    @Override
    public void accept(Visitor visitor) {
      visitor.visit(this);
    }

    public String toString() {
      return String.format("ForEach:%s", this.data);
    }
  }
}
