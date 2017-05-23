package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.Utils;

import static com.github.dakusui.actionunit.Checks.checkNotNull;

/**
 * A skeletal base class of a simple action, which cannot be divided into smaller
 * actions.
 * Any action that actually does any concrete operation outside "ActionUnit"
 * framework should extend this class.
 */
public abstract class Leaf extends ActionBase {
  public Leaf() {
  }

  public static Action create(Runnable runnable) {
    checkNotNull(runnable);
    return new Leaf() {
      @Override
      public void perform() {
        runnable.run();
      }

      @Override
      public String toString() {
        return Utils.describe(runnable);
      }
    };
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  abstract public void perform();
}
