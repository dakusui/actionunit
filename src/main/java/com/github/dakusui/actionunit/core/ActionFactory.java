package com.github.dakusui.actionunit.core;

import java.util.function.Supplier;

@FunctionalInterface
public interface ActionFactory extends Context, Supplier<Action> {
  default Action get() {
    ID_GENERATOR_MANAGER.reset(this);
    return create();
  }

  default Action create() {
    return create(this);
  }

  Action create(ActionFactory self);
}
