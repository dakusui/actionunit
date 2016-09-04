package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.Action;

/**
 * An interface that represents a sequence of actions to be executed concurrently.
 */
public interface Concurrent extends Composite {
  /**
   * A class that represents a collection of actions that should be executed concurrently.
   */
  class Base extends Composite.Base implements com.github.dakusui.actionunit.actions.Concurrent {
    public Base(Iterable<? extends Action> actions) {
      super("Concurrent", actions);
    }

    @Override
    public void accept(Visitor visitor) {
      visitor.visit(this);
    }
  }

  enum Factory implements Composite.Factory {
    INSTANCE;

    @Override
    public Concurrent create(Iterable<? extends Action> actions) {
      return new Base(actions);
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
      return "Concurrent";
    }
  }
}
