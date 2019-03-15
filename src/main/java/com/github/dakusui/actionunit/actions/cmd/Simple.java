package com.github.dakusui.actionunit.actions.cmd;

public class Simple extends Commodore<Simple> {
  public Simple(CommandLineComposerFactory commandLineComposerFactory) {
    super(commandLineComposerFactory);
  }
  @Override
  public com.github.dakusui.actionunit.actions.cmd.Simple command(String commandLineString, String... variableNames) {
    return this;
  }
}
