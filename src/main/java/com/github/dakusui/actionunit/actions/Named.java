package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.Action;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An action that has a name.
 */
public interface Named extends Action {
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
  class Base extends ActionBase implements com.github.dakusui.actionunit.actions.Named {
    private final String name;
    private final Action action;

    /**
     * Creates an object of this class.
     *
     * @param name   Name of this object.
     * @param action Action to be performed as a body of this object.
     */
    public Base(String name, Action action) {
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

  /**
   * A factory that creates {@link com.github.dakusui.actionunit.actions.Named} action object.
   */
  enum Factory {
    ;

    /**
     * Creates an action with the given {@code name} and {@code action}.
     *
     * @param name   A name of the returned action.
     * @param action An action body of the returned action.
     */
    public static com.github.dakusui.actionunit.actions.Named create(String name, Action action) {
      return new Base(name, action);
    }
  }
}
