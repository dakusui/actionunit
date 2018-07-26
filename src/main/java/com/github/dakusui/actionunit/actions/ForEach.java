package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;

import java.util.Formattable;
import java.util.Formatter;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public interface ForEach<E> extends Action, Formattable {
  Stream<E> data();

  Mode getMode();

  <T> Action createHandler(ValueHolder<T> valueHolder);

  enum Mode {
    SEQUENTIALLY,
    CONCURRENTLY
  }

  @Override
  default void formatTo(Formatter formatter, int flags, int width, int precision) {
    formatter.format("ForEach %s %s", this.getMode());
  }

  class Impl<E> extends ActionBase implements ForEach<E> {
    private final Supplier<Stream<E>> data;
    private final Mode                mode;
    private final String              variableName;

    public Impl(int id, String variableName, Supplier<Stream<E>> data, Mode mode) {
      super(id);
      this.variableName = requireNonNull(variableName);
      this.data = requireNonNull(data);
      this.mode = requireNonNull(mode);
    }

    @Override
    public Stream<E> data() {
      return this.data.get();
    }

    @Override
    public Mode getMode() {
      return this.mode;
    }

    @Override
    public <T> Action createHandler(ValueHolder<T> valueHolder) {
      return null;
    }

    @Override
    public void accept(Visitor visitor) {

    }

    @Override
    public int id() {
      return 0;
    }
  }
}
