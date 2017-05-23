package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.Action;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface HandlerFactory<T> extends Function<Supplier<T>, Action> {
  static <T> HandlerFactory<T> create(Consumer<T> handlerBody) {
    return data -> Leaf.create(() -> handlerBody.accept(data.get()));
  }
}
