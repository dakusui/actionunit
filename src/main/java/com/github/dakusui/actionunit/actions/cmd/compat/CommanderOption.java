package com.github.dakusui.actionunit.actions.cmd.compat;

import com.github.dakusui.actionunit.actions.cmd.CommanderUtils;
import com.github.dakusui.actionunit.actions.cmd.compat.CompatCommander;

public interface CommanderOption {
  String longFormat();

  String shortFormat();

  default String format(boolean longFormat) {
    return longFormat ? longFormat() : shortFormat();
  }

  @SuppressWarnings("unchecked")
  default <B extends CompatCommander> B addTo(B builder, String value, boolean longFormat) {
    return (B) builder.add(String.format("%s %s", this.format(longFormat), CommanderUtils.quoteWithSingleQuotesForShell(value)));
  }

  @SuppressWarnings("unchecked")
  default <B extends CompatCommander> B addTo(B builder, boolean longFormat) {
    return (B) builder.add(this.format(longFormat));
  }
}
