package com.github.dakusui.actionunit;

public interface TestOutput {
  class Text implements TestOutput {
    private final String value;

    public Text(String value) {
      this.value = value;
    }

    public String value() {
      return this.value;
    }
  }
}
