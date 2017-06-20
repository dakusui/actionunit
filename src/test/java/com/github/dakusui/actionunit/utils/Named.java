package com.github.dakusui.actionunit.utils;

public interface Named {
  default String name() {
    return "noname";
  }
}
