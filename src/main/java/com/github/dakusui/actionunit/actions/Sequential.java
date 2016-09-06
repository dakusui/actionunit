package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.Action;

/**
 * An interface that represents a sequence of actions to be executed one
 * after another.
 */
public interface Sequential extends Composite {
  /**
   * An implementation of {@link Sequential} action.
   */
  class Impl extends Base implements Sequential {
    /**
     * Creates an object of this class.
     *
     * @param actions Actions to be executed by this object.
     */
    public Impl(Iterable<? extends Action> actions) {
      super("Sequential", actions);
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
  }

  /**
   * A factory for {@link Sequential} action object.
   */
  enum Factory implements Composite.Factory {
    INSTANCE;

    @Override
    public Sequential create(Iterable<? extends Action> actions) {
      return new Impl(actions);
    }

    public String toString() {
      return "Sequential";
    }
  }
}
