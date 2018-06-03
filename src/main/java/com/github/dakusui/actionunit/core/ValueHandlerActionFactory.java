package com.github.dakusui.actionunit.core;

import com.github.dakusui.actionunit.actions.Leaf;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface ValueHandlerActionFactory<T> extends Function<Supplier<T>, Action>, Context, Cloneable {
  static <T> ValueHandlerActionFactory<T> create(String description, Consumer<T> handlerBody) {
    Objects.requireNonNull(handlerBody);
    return new ValueHandlerActionFactory<T>() {
      @Override
      public Action create(Context context, Supplier<T> data) {
        return Leaf.create(this.generateId(), description, () -> handlerBody.accept(data.get()));
      }
    };
  }

  default Action apply(Supplier<T> data) {
    ID_GENERATOR_MANAGER.reset(this);
    return create(this, data);
  }

  Action create(Context factory, Supplier<T> data);
}
