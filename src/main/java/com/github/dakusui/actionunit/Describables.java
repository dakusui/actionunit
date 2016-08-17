package com.github.dakusui.actionunit;

import static com.google.common.base.Throwables.propagate;

public enum Describables {
  ;

  /**
   * Tries to describe given {@code obj} in a best possible way.
   *
   * @param obj An object to be described.
   */
  public static String describe(Object obj) {
    if (obj == null) {
      return "null";
    }
    if (obj instanceof Describable) {
      return ((Describable) obj).describe();
    }
    try {
      if (obj.getClass().getMethod("toString").equals(Object.class.getMethod("toString"))) {
        return Utils.shortClassNameOf(obj.getClass());
      }
    } catch (NoSuchMethodException e) {
      throw propagate(e);
    }
    return obj.toString();
  }
}
