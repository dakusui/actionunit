package com.github.dakusui.actionunit.actions.cmd.linux;

import com.github.dakusui.actionunit.actions.cmd.Commander;
import com.github.dakusui.actionunit.core.context.ContextFunction;

import java.util.function.IntFunction;

public class Echo extends Commander<Echo> {
  public Echo(IntFunction<String> parameterPlaceHolderFormatter) {
    super(parameterPlaceHolderFormatter);
    this.command("/bin/echo");
  }

  public Echo noTrailingNewLine() {
    return this.append(" ").append("-n");
  }

  public Echo enableBackslashInterpretation() {
    return this.append(" ").append("-e");
  }

  public Echo disableBackslashInterpretation() {
    return this.append(" ").append("-E");
  }

  public Echo message(String message) {
    return this.add(message);
  }

  public Echo message(ContextFunction<String> message) {
    return this.add(message);
  }
}
