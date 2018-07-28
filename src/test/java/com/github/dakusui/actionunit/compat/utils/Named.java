package com.github.dakusui.actionunit.compat.utils;

public interface Named {
  default String name() {
    return "noname";
  }
}
