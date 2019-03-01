package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.processstreamer.core.process.Shell;

import static java.util.Objects.requireNonNull;

public class BaseCommander<C extends BaseCommander<C>> extends Commander<C> {
  private CommandLineComposer commandLineComposer;
  private String[]            variableNames;

  public BaseCommander(Shell shell) {
    super(shell);
  }

  public C command(String commandLineFormat, String... variableNames) {
    requireNonNull(commandLineFormat);
    return command(() -> commandLineFormat, variableNames);
  }

  @SuppressWarnings("unchecked")
  public C command(CommandLineComposer commandLineComposer, String... variableNames) {
    this.commandLineComposer = requireNonNull(commandLineComposer);
    this.variableNames = variableNames;
    return (C) this;
  }

  protected CommandLineComposer commandLineComposer() {
    return this.commandLineComposer;
  }

  @Override
  protected String[] variableNames() {
    return this.variableNames;
  }
}
