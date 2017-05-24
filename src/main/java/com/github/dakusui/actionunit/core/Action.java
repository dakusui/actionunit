package com.github.dakusui.actionunit.core;

import com.github.dakusui.actionunit.actions.*;
import com.github.dakusui.actionunit.compat.actions.CompatAttempt;
import com.github.dakusui.actionunit.compat.actions.CompatForEach;
import com.github.dakusui.actionunit.compat.actions.CompatWith;
import com.github.dakusui.actionunit.compat.actions.Tag;

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
  interface Visitor {
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
    void visit(Leaf action);

    /**
     * Visits an {@code action}.
     *
     * @param action action to be visited by this object.
     */
    void visit(Named action);

    /**
     * Visits an {@code action}.
     *
     * @param action action to be visited by this object.
     */
    void visit(Composite action);

    /**
     * Visits an {@code action}.
     *
     * @param action action to be visited by this object.
     */
    void visit(Sequential action);

    /**
     * Visits an {@code action}.
     *
     * @param action action to be visited by this object.
     */
    void visit(Concurrent action);

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
    <T> void visit(ForEach<T> action);

    /**
     * Visits an {@code action}.
     *
     * @param action action to be visited by this object.
     */
    <E extends Throwable> void visit(Attempt<E> action);

    /**
     * Visits an {@code action}.
     *
     * @param action action to be visited by this object.
     */
    void visit(TestAction action);

    /**
     * Visits an {@code action}.
     *
     * @param action action to be visited by this object.
     */
    void visit(While action);

    /**
     * Visits an {@code action}.
     *
     * @param action action to be visited by this object.
     */
    void visit(Tag action);

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
    void visit(Retry action);

    /**
     * Visits an {@code action}.
     *
     * @param action action to be visited by this object.
     */
    void visit(TimeOut action);

    /**
     * Visits an {@code action}.
     *
     * @param action action to be visited by this object.
     */
    void visit(CompatAttempt action);

    void visit(When when);

    abstract class Base implements Visitor {

      protected Base() {
      }

      @Override
      public void visit(Leaf action) {
        this.visit((Action) action);
      }

      @Override
      public void visit(Named action) {
        this.visit((Action) action);
      }

      @Override
      public void visit(Composite action) {
        this.visit((Action) action);
      }

      @Override
      public void visit(Sequential action) {
        this.visit((Composite) action);
      }

      @Override
      public void visit(Concurrent action) {
        this.visit((Composite) action);
      }

      @Override
      public void visit(CompatForEach action) {
        this.visit((Action) action);
      }

      @Override
      public <T> void visit(ForEach<T> action) {
        this.visit((Action) action);
      }

      @Override
      public <T extends Throwable> void visit(Attempt<T> action) {
        this.visit((Action) action);
      }

      @Override
      public void visit(TestAction action) {
        this.visit((Action) action);
      }

      @Override
      public void visit(While action) {
        this.visit((Action) action);
      }

      @Override
      public void visit(Tag action) {
        this.visit((Action) action);
      }

      @Override
      public void visit(CompatWith action) {
        this.visit((Action) action);
      }


      @Override
      public void visit(When action) {
        this.visit((Action) action);
      }

      @Override
      public void visit(Retry action) {
        this.visit((Action) action);
      }

      @Override
      public void visit(TimeOut action) {
        this.visit((Action) action);
      }

      @Override
      public void visit(CompatAttempt action) {
        this.visit((Action) action);
      }

    }
  }

  /**
   * This interface is used to let path calculation know an action is synthesized
   * by another and the creator action should be taken into account in the calculation,
   * instead of itself.
   */
  interface Synthesized {
    Action getParent();
  }
}
