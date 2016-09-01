package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.Utils;

/**
 * A skeletal base class of all {@code Action}s.
 */
public abstract class ActionBase implements Action {
  protected String formatClassName() {
    return Utils.shortClassNameOf(this.getClass()).replaceAll("^Action\\$", "").replaceAll("\\$Base$", "");
  }

  @Override
  public String toString() {
    return this.formatClassName();
  }
}
