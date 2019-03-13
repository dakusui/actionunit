package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.processstreamer.core.process.Shell;

import static java.util.Objects.requireNonNull;

public class CommanderImpl extends CommanderBase<CommanderImpl> {

  private final Commodore.CommandLineComposerFactory commandLineComposerFactory;
  private       CommandLineComposer                  commandLineComposer;
  private       String[]                             variableNames;

  public CommanderImpl(Shell shell, Commodore.CommandLineComposerFactory commandLineComposerFactory) {
    super(shell);
    this.commandLineComposerFactory = requireNonNull(commandLineComposerFactory);
  }

  public CommanderImpl command(String commandLineFormat, String... variableNames) {
    requireNonNull(commandLineFormat);
    return command(this.commandLineComposerFactory.apply(commandLineFormat, variableNames), variableNames);
  }

  @Override
  protected CommandLineComposer commandLineComposer() {
    return this.commandLineComposer;
  }

  @Override
  protected String[] variableNames() {
    return this.variableNames;
  }

  private CommanderImpl command(CommandLineComposer commandLineComposer, String... variableNames) {
    this.commandLineComposer = requireNonNull(commandLineComposer);
    this.variableNames = variableNames;
    return this;
  }
}
