package com.github.dakusui.actionunit.n.actions;

import com.github.dakusui.actionunit.n.core.Action;

import static java.util.Objects.requireNonNull;

public interface Named extends Action {
  String name();

  Action action();

  @Override
  default void accept(Visitor visitor) {
    visitor.visit(this);
  }

  static Action of(String name, Action action) {
    requireNonNull(name);
    requireNonNull(action);
    return new Named() {
      @Override
      public String name() {
        return name;
      }

      @Override
      public Action action() {
        return action;
      }

    };
  }
}
