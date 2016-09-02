package com.github.dakusui.actionunit.tests.actions;

/**
 * A skeletal base class of a simple action, which cannot be divided into smaller
 * actions.
 * Any action that actually does any concrete operation outside "ActionUnit"
 * framework should extend this class.
 */
public abstract class Leaf extends ActionBase {
  public Leaf() {
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  abstract public void perform();
}
