package com.github.dakusui.actionunit.core;

import com.github.dakusui.actionunit.actions.Leaf;
import com.github.dakusui.actionunit.actions.ValueHolder;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.github.dakusui.actionunit.core.IdGeneratorManager.ID_GENERATOR_MANAGER;

@Deprecated
public interface ValueHandlerActionFactory<T> extends Function<ValueHolder<T>, Action>, Context, Cloneable {
  static <T> ValueHandlerActionFactory<T> create(String description, Consumer<T> handlerBody) {
    Objects.requireNonNull(handlerBody);
    return new ValueHandlerActionFactory<T>() {
      private Bean bean = new Bean();

      @Override
      public Bean bean() {
        return this.bean;
      }

      @Override
      public Action create(Context context, ValueHolder<T> valueHolder) {
        return Leaf.create(this.generateId(), description, () -> handlerBody.accept(valueHolder.get()));
      }
    };
  }

  default Action apply(ValueHolder<T> valueHolder) {
    ID_GENERATOR_MANAGER.reset(this);
    return create(this, valueHolder);
  }

  default AtomicInteger idGenerator() {
    return ID_GENERATOR_MANAGER.idGenerator(this);
  }

  @Override
  default  Bean bean() {
    throw new UnsupportedOperationException();
  }

  Action create(Context factory, ValueHolder<T> valueHolder);

}
