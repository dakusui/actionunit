package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.Action;

import static com.github.dakusui.actionunit.Checks.checkNotNull;

/**
 * A skeletal base class of a simple action, which cannot be divided into smaller
 * actions.
 * Any action that actually does any concrete operation outside "ActionUnit"
 * framework should extend this class.
 */
public abstract class Leaf extends ActionBase {
  protected Leaf() {
  }

  public static Action create(String description, Runnable runnable) {
    checkNotNull(description);
    checkNotNull(runnable);
    return new Leaf() {
      @Override
      public void perform() {
        runnable.run();
      }

      @Override
      public String toString() {
        return description;
      }
    };
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  abstract public void perform();
}
