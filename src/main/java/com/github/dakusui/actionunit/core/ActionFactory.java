package com.github.dakusui.actionunit.core;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@Deprecated
@FunctionalInterface
public interface ActionFactory extends Context, Supplier<Action> {
  IdGeneratorManager ID_GENERATOR_MANAGER = new IdGeneratorManager();
  default Action get() {
    ID_GENERATOR_MANAGER.reset(this);
    return create();
  }

  default Action create() {
    return create(this);
  }

  default AtomicInteger idGenerator() {
    return ID_GENERATOR_MANAGER.idGenerator(this);
  }

  default <T> Bean<T> bean() {
    throw new UnsupportedOperationException("This shouldn't be called");
  }

  Action create(ActionFactory self);
}
