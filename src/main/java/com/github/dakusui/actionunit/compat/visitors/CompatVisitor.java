package com.github.dakusui.actionunit.compat.visitors;

import com.github.dakusui.actionunit.actions.CompatWhile;
import com.github.dakusui.actionunit.actions.CompatWhen;
import com.github.dakusui.actionunit.compat.actions.CompatAttempt;
import com.github.dakusui.actionunit.compat.actions.CompatForEach;
import com.github.dakusui.actionunit.compat.actions.CompatWith;
import com.github.dakusui.actionunit.compat.actions.Tag;
import com.github.dakusui.actionunit.core.Action;

public interface CompatVisitor {
  /**
   * Visits an {@code action}.
   *
   * @param action action to be visited by this object.
   */
  void visit(Action action);

  /**
   * Visits an {@code action}.
   *
   * @param action action to be visited by this object.
   */
  default void visit(CompatForEach action) {
    this.visit((Action) action);
  }

  /**
   * Visits an {@code action}.
   *
   * @param action action to be visited by this object.
   */
  default <T> void visit(CompatWhile<T> action) {
    this.visit((Action) action);
  }

  /**
   * Visits an {@code action}.
   *
   * @param action action to be visited by this object.
   */
  default <T> void visit(CompatWhen<T> action) {
    this.visit((Action) action);
  }

  /**
   * Visits an {@code action}.
   *
   * @param action action to be visited by this object.
   */
  default <T> void visit(CompatWith<T> action) {
    this.visit((Action) action);
  }

  /**
   * Visits an {@code action}.
   *
   * @param action action to be visited by this object.
   */
  default void visit(CompatAttempt action) {
    this.visit((Action) action);
  }

  /**
   * Visits an {@code action}.
   *
   * @param action action to be visited by this object.
   */
  default void visit(Tag action) {
    this.visit((Action) action);
  }

  ;

  /**
   * This interface is used to let path calculation know an action is synthesized
   * by another and the creator action should be taken into account in the calculation,
   * instead of itself.
   */
  interface Synthesized {
    Action getParent();
  }
}
