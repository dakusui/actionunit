package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.core.Context;

import java.util.concurrent.atomic.AtomicInteger;

import static com.github.dakusui.actionunit.core.ActionFactory.ID_GENERATOR_MANAGER;

public interface UtContext extends Context {
  default AtomicInteger idGenerator() {
    return ID_GENERATOR_MANAGER.idGenerator(this);
  }

  @Override
  default <T> Bean<T> bean() {
    throw new UnsupportedOperationException("This method shouldn't be called.");
  }
}
