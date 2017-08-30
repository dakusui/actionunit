package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.helpers.InternalUtils;

/**
 * A skeletal base class of all {@code Action}s.
 */
public abstract class ActionBase implements Action {
  private final int id;

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
