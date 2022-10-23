package com.github.dakusui.actionunit.actions.cmd.unix;

import com.github.dakusui.actionunit.actions.cmd.Commander;
import com.github.dakusui.actionunit.actions.cmd.CommanderInitializer;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.context.ContextFunction;

import java.util.function.Function;

public class Echo extends Commander<Echo> {
  public Echo(CommanderInitializer initializer) {
    super(initializer);
    initializer.init(this);
  }

  public Echo noTrailingNewLine() {
    return this.append(" ").append("-n");
  }

  public Echo enableBackslashInterpretation() {
    return this.addOption("-e");
  }

  public Echo disableBackslashInterpretation() {
    return this.addOption("-E");
  }

  public Echo message(String message) {
    return this.add(message);
  }

  public Echo message(Function<Context, String> message) {
    return this.add(message);
  }
}
