package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;

import java.util.Formatter;

import static java.util.Objects.requireNonNull;

/**
 * An action that gives a name to another.
 */
public interface Named extends Action<Named> {
  /**
   * Returns a name of this action.
   *
   * @return name of this object.
   */
  String name();

  /**
   * Returns an action named by this object.
   *
   * @return An action named by this.
   */
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
