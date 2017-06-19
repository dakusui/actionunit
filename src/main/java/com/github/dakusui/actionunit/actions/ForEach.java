package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.ValueHandlerFactory;

import java.util.Objects;
import java.util.function.Supplier;

import static com.github.dakusui.actionunit.helpers.InternalUtils.describe;
import static com.github.dakusui.actionunit.helpers.InternalUtils.summary;

public interface ForEach<T> extends Action {
  Iterable<T> data();

  Action createHandler(Supplier<T> data);

  Mode getMode();

  static <E> ForEach.Builder<E> builder(int id, Iterable<? extends E> elements) {
    return new ForEach.Builder<>(id, elements);
  }

  class Builder<E> {
    private final Iterable<? extends E> elements;
    private final int                   id;
    private Mode mode = Mode.SEQUENTIALLY;

    Builder(int id, Iterable<? extends E> elements) {
      this.id = id;
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

    public ForEach<E> perform(ValueHandlerFactory<E> operation) {
      Objects.requireNonNull(operation);
      Objects.requireNonNull(operation);
      //noinspection unchecked
      return new ForEach.Impl<>(
          id,
          operation,
          (Iterable<E>) this.elements,
          this.mode
      );
    }
  }

  enum Mode {
    SEQUENTIALLY,
    CONCURRENTLY
  }

  class Impl<T> extends ActionBase implements ForEach<T> {
    private final ValueHandlerFactory<T> handlerFactory;
    private final Iterable<T>            data;
    private final Mode                   mode;

    public Impl(int id, ValueHandlerFactory<T> handlerFactory, Iterable<T> data, Mode mode) {
      super(id);
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

    @Override
    public String toString() {
      return String.format("%s (%s) %s", super.toString(), mode, summary(describe(this.data)));
    }
  }
}
