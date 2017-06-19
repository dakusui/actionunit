package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.ActionFactory;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface HandlerFactory<T> extends Function<Supplier<T>, Action>, ActionFactory, Cloneable {
  static <T> HandlerFactory<T> create(String description, Consumer<T> handlerBody) {
    Objects.requireNonNull(handlerBody);
    return new HandlerFactory<T>() {
      @Override
      public Action create(ActionFactory factory, Supplier<T> data) {
        return Leaf.create(this.generateId(), description, () -> handlerBody.accept(data.get()));
      }
    };
  }

  default Action apply(Supplier<T> data) {
    ID_GENERATOR_MANAGER.reset(this);
    return create(this, data);
  }

  Action create(ActionFactory factory, Supplier<T> data);

  abstract class Base<T> implements HandlerFactory<T> {
    private final ThreadLocal<AtomicInteger> idGenerator = new ThreadLocal<>();

    @Override
    public int generateId() {
      if (idGenerator.get() == null)
        idGenerator.set(new AtomicInteger(0));
      return idGenerator.get().getAndIncrement();
    }

    @Override
    final public Action apply(Supplier<T> data) {
      if (idGenerator.get() != null)
        idGenerator.get().set(0);
      return create(this, data);
    }
  }
}
