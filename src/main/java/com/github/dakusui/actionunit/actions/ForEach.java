package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.ValueHandlerActionFactory;

import static com.github.dakusui.actionunit.helpers.InternalUtils.describe;
import static com.github.dakusui.actionunit.helpers.InternalUtils.summary;
import static java.util.Objects.requireNonNull;

public interface ForEach<T> extends Action {
  Iterable<T> data();

  Action createHandler(ValueHolder<T> data);

  Mode getMode();

  ValueHolder<T> defaultValue();

  static <E> ForEach.Builder<E> builder(int id, Iterable<? extends E> elements) {
    return new ForEach.Builder<>(id, elements);
  }

  class Builder<E> {
    private final Iterable<? extends E> elements;
    private final int                   id;
    private Mode mode = Mode.SEQUENTIALLY;
    private ValueHolder<E> defaultValue;

    Builder(int id, Iterable<? extends E> elements) {
      this.id = id;
      this.elements = requireNonNull(elements);
      this.defaultValue = ValueHolder.empty();
    }

    public Builder<E> withDefault(E defaultValue) {
      this.defaultValue = ValueHolder.of(defaultValue);
      return this;
    }

    public Builder<E> sequentially() {
      this.mode = Mode.SEQUENTIALLY;
      return this;
    }

    public Builder<E> concurrently() {
      this.mode = Mode.CONCURRENTLY;
      return this;
    }

    public ForEach<E> perform(ValueHandlerActionFactory<E> operation) {
      requireNonNull(operation);
      //noinspection unchecked
      return new ForEach.Impl<>(
          id,
          operation,
          (Iterable<E>) this.elements,
          this.mode,
          defaultValue);
    }

    public ForEach<E> perform(Action action) {
      requireNonNull(action);
      return perform((factory, data) -> action);
    }

  }

  enum Mode {
    SEQUENTIALLY,
    CONCURRENTLY
  }

  class Impl<T> extends ActionBase implements ForEach<T> {
    private final ValueHandlerActionFactory<T> handlerFactory;
    private final Iterable<T>                  data;
    private final Mode                         mode;
    private final ValueHolder<T>               defaultValue;

    public Impl(int id, ValueHandlerActionFactory<T> handlerFactory, Iterable<T> data, Mode mode, ValueHolder<T> defaultValue) {
      super(id);
      this.handlerFactory = requireNonNull(handlerFactory);
      this.data = requireNonNull(data);
      this.mode = requireNonNull(mode);
      this.defaultValue = requireNonNull(defaultValue);
    }

    @Override
    public Iterable<T> data() {
      return this.data;
    }

    @Override
    public Action createHandler(ValueHolder<T> data) {
      return this.handlerFactory.apply(data);
    }

    @Override
    public Mode getMode() {
      return this.mode;
    }

    @Override
    public ValueHolder<T> defaultValue() {
      return defaultValue;
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
