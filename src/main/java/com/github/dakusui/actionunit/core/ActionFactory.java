package com.github.dakusui.actionunit.core;

@FunctionalInterface
public interface ActionFactory extends Context {
  default Action create() {
    return create(this);
  }

  @Override
  default int generateId() {
    return 0;
  }

  default Bean bean() {
    throw new UnsupportedOperationException("This shouldn't be called");
  }

  Action create(Context self);
}
