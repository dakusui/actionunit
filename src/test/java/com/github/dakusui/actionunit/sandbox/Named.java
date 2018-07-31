package com.github.dakusui.actionunit.sandbox;

public interface Named {
  default String name() {
    return "noname";
  }
}
