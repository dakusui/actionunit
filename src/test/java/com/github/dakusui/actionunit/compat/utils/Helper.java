package com.github.dakusui.actionunit.compat.utils;

public interface Helper {
  static void staticMethod() {
    System.out.println("staticMethodInInterface");
  }

  default void defaultMethod() {
    System.out.println("defaultMethodInInterface");
  }


  default void overriddenDefaultMethod() {
    System.out.println("overriddenDefaultMethodInInterface");
  }
}
