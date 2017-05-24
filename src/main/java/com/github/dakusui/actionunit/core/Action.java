package com.github.dakusui.actionunit.core;

import com.github.dakusui.actionunit.actions.*;
import com.github.dakusui.actionunit.compat.visitors.CompatVisitor;

/**
 * Defines interface of an action performed by ActionUnit runner.
 */
public interface Action {

  /**
   * Applies a visitor to this element.
   *
   * @param visitor the visitor operating on this element.
   */
  void accept(Visitor visitor);


  /**
   * A visitor of actions, in the style of the visitor design pattern. Classes implementing
   * this interface are used to operate on an action when the kind of element is unknown at compile
   * time. When a visitor is passed to an element's accept method, the visitXYZ method most applicable
   * to that element is invoked.
   * <p/>
   * WARNING: It is possible that methods will be added to this interface to accommodate new, currently
   * unknown, language structures added to future versions of the ActionUnit library. Therefore,
   * visitor classes directly implementing this interface may be source incompatible with future
   * versions of the framework.
   * To avoid this source incompatibility, visitor implementations are encouraged to
   * instead extend the appropriate abstract visitor class that implements this interface. However,
   * an API should generally use this visitor interface as the type for parameters, return type, etc.
   * rather than one of the abstract classes.
   *
   * @see Base
   */
  interface Visitor extends CompatVisitor {
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
    default void visit(Leaf action) {
      this.visit((Action) action);
    }

    /**
     * Visits an {@code action}.
     *
     * @param action action to be visited by this object.
     */
    default void visit(Named action) {
      this.visit((Action) action);
    }

    /**
     * Visits an {@code action}.
     *
     * @param action action to be visited by this object.
     */
    default void visit(Composite action) {
      this.visit((Action) action);
    }

    /**
     * Visits an {@code action}.
     *
     * @param action action to be visited by this object.
     */
    default void visit(Sequential action) {
      this.visit((Composite) action);
    }

    /**
     * Visits an {@code action}.
     *
     * @param action action to be visited by this object.
     */
    default void visit(Concurrent action) {
      this.visit((Composite) action);
    }

    /**
     * Visits an {@code action}.
     *
     * @param action action to be visited by this object.
     */
    default <T> void visit(ForEach<T> action) {
      this.visit((Action) action);
    }

    /**
     * Visits an {@code action}.
     *
     * @param action action to be visited by this object.
     */
    default <T> void visit(While<T> action) {
      this.visit((Action) action);
    }

    /**
     * Visits an {@code action}.
     *
     * @param action action to be visited by this object.
     */
    default <T> void visit(When<T> action) {
      this.visit((Action) action);
    }

    /**
     * Visits an {@code action}.
     *
     * @param action action to be visited by this object.
     */
    default <T extends Throwable> void visit(Attempt<T> action) {
      this.visit((Action) action);
    }

    /**
     * Visits an {@code action}.
     *
     * @param action action to be visited by this object.
     */
    default void visit(TestAction action) {
      this.visit((Action) action);
    }

    /**
     * Visits an {@code action}.
     *
     * @param action action to be visited by this object.
     */
    default void visit(Retry action) {
      this.visit((Action) action);
    }

    /**
     * Visits an {@code action}.
     *
     * @param action action to be visited by this object.
     */
    default void visit(TimeOut action) {
      this.visit((Action) action);
    }

    abstract class Base implements Visitor {
      protected Base() {
      }
    }
  }
}