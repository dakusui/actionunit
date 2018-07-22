package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.generators.ActionGenerator;

import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.github.dakusui.actionunit.helpers.InternalUtils.describe;
import static com.github.dakusui.actionunit.helpers.InternalUtils.summary;
import static java.util.Objects.requireNonNull;

public interface ForEach<T> extends Action {
  Stream<? extends T> data();

  Action createHandler(ValueHolder<T> data);

  Mode getMode();

  ValueHolder<T> defaultValue();

  static <E> ForEach.Builder<E> builder(int id, Supplier<Stream<E>> streamSupplier) {
    return new ForEach.Builder<>(id, streamSupplier);
  }

  class Builder<E> {
    private final Supplier<Stream<E>> elements;
    private final int                           id;
    private       Mode                          mode = Mode.SEQUENTIALLY;
    private       ValueHolder<E>                defaultValue;

    Builder(int id, Supplier<Stream<E>> elements) {
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

    public ForEach<E> perform(ActionGenerator<E> operation) {
      return new ForEach.Impl<>(
          id,
          requireNonNull(operation),
          this.elements,
          this.mode,
          defaultValue);
    }

    public ForEach<E> perform(Action action) {
      return perform(ActionGenerator.from(requireNonNull(action)));
    }
  }

  enum Mode {
    SEQUENTIALLY,
    CONCURRENTLY
  }

  class Impl<T> extends ActionBase implements ForEach<T> {
    private final ActionGenerator<T>            handlerFactory;
    private final Supplier<Stream<T>> data;
    private final Mode                          mode;
    private final ValueHolder<T>                defaultValue;

    protected Impl(int id, ActionGenerator<T> handlerFactory, Supplier<Stream<T>> data, Mode mode, ValueHolder<T> defaultValue) {
      super(id);
      requireNonNull(handlerFactory);
      this.handlerFactory = handlerFactory;
      this.data = requireNonNull(data);
      this.mode = requireNonNull(mode);
      this.defaultValue = requireNonNull(defaultValue);
    }

    @Override
    public Stream<? extends T> data() {
      return this.data.get();
    }

    @Override
    public Action createHandler(ValueHolder<T> data) {
      return this.handlerFactory.apply(data, Context.create());
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
