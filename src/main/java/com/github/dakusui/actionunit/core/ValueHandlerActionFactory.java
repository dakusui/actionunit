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
