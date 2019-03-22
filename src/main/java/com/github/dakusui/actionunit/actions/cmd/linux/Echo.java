package com.github.dakusui.actionunit.actions.cmd.linux;

import com.github.dakusui.actionunit.actions.cmd.Commander;

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
    return this.message(message, false);
  }

  public Echo message(String message, boolean quoted) {
    this.append(" ");
    return quoted ?
        this.appendq(message) :
        this.append(message);
  }
}
