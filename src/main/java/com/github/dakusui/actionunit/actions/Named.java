package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;

import static com.github.dakusui.actionunit.helpers.Checks.checkNotNull;

/**
 * An action that has a name.
 */
public interface Named extends Nested {
  /**
   * Creates an action with the given {@code name} and {@code action}.
   *
   * @param name   A name of the returned action.
   * @param action An action body of the returned action.
   */
  static Named create(String name, Action action) {
    return new Impl(name, action);
  }

  /**
   * Returns a name of this action.
   */
  String getName();

  /**
   * Returns an action named by this object.
   */
  Action getAction();

  /**
   * A skeletal base class to implement {@code Named} action.
   */
  class Impl extends ActionBase implements Named {
    private final String name;
    private final Action action;

    /**
     * Creates an object of this class.
     *
     * @param name   Name of this object.
     * @param action Action to be performed as a body of this object.
     */
    public Impl(String name, Action action) {
      this.name = checkNotNull(name);
      this.action = checkNotNull(action);
    }

    /**
     * {@inheritDoc}
     *
     * @param visitor the visitor operating on this element.
     */
    @Override
    public void accept(Visitor visitor) {
      visitor.visit(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
      return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Action getAction() {
      return action;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
      return this.getName();
    }
  }
}
