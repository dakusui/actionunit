package com.github.dakusui.actionunit.compat.actions;

import com.github.dakusui.actionunit.compat.core.Action;
import com.github.dakusui.actionunit.compat.utils.InternalUtils;

/**
 * A skeletal base class of all {@code Action}s.
 */
public abstract class ActionBase implements Action {
  private final int             id;

  public ActionBase(int id) {
    this.id = id;
  }

  @Override
  public int id() {
    return this.id;
  }

  @Override
  public String toString() {
    return this.formatClassName();
  }

  String formatClassName() {
    return InternalUtils.shortClassNameOf(this.getClass()).replaceAll("^Action\\$", "").replaceAll("\\$Base$", "").replaceAll("\\$Impl$", "");
  }
}
