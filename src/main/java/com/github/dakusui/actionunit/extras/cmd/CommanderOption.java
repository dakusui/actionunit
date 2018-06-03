package com.github.dakusui.actionunit.extras.cmd;

public interface CommanderOption {
  String longFormat();

  String shortFormat();

  default String format(boolean longFormat) {
    return longFormat ? longFormat() : shortFormat();
  }

  @SuppressWarnings("unchecked")
  default <B extends Commander> B addTo(B builder, String value, boolean longFormat) {
    return (B) builder.add(String.format("%s %s", this.format(longFormat), CommanderUtils.quoteWithSingleQuotesForShell(value)));
  }

  @SuppressWarnings("unchecked")
  default <B extends Commander> B addTo(B builder, boolean longFormat) {
    return (B) builder.add(this.format(longFormat));
  }
}
