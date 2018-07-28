package com.github.dakusui.actionunit.n.actions;

import com.github.dakusui.actionunit.n.core.Action;

import java.util.Formattable;
import java.util.Formatter;

import static java.util.Objects.requireNonNull;

public interface Named extends Action {
  String name();

  Action action();

  @Override
  default void formatTo(Formatter formatter, int flags, int width, int precision) {
    formatter.format(name());
  }

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
