package com.github.dakusui.actionunit.compat;

import com.github.dakusui.actionunit.compat.actions.CompatAttempt;
import com.github.dakusui.actionunit.compat.actions.CompatForEach;
import com.github.dakusui.actionunit.compat.actions.CompatWith;

public interface CompatVisitor {
  /**
   * Visits an {@code action}.
   *
   * @param action action to be visited by this object.
   */
  void visit(CompatForEach action);

  /**
   * Visits an {@code action}.
   *
   * @param action action to be visited by this object.
   */
  void visit(CompatWith action);

  /**
   * Visits an {@code action}.
   *
   * @param action action to be visited by this object.
   */
  void visit(CompatAttempt action);
}
