package com.github.dakusui.actionunit;

public enum Describables {
  ;
  public static String describe(Object obj) {
    if (obj == null) {
      return "null";
    }
    if (obj instanceof Describable) {
      return ((Describable) obj).describe();
    }
    return obj.toString();
  }
}
