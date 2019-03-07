package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.processstreamer.core.process.Shell;

import static java.util.Objects.requireNonNull;

public class Commander extends AbstractCommander<Commander> {
  private CommandLineComposer commandLineComposer;
  private String[]            variableNames;

  public Commander(Shell shell) {
    super(shell);
  }

  public Commander command(String commandLineFormat, String... variableNames) {
    requireNonNull(commandLineFormat);
    return command(() -> commandLineFormat, variableNames);
  }

  @SuppressWarnings("unchecked")
  public Commander command(CommandLineComposer commandLineComposer, String... variableNames) {
    this.commandLineComposer = requireNonNull(commandLineComposer);
    this.variableNames = variableNames;
    return this;
  }

  public Commander cmd(String commandLineFormat, String... variableNames) {
    return this;
  }

  @Override
  protected CommandLineComposer commandLineComposer() {
    return this.commandLineComposer;
  }

  @Override
  protected String[] variableNames() {
    return this.variableNames;
  }

}
