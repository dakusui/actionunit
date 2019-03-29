package com.github.dakusui.actionunit.actions.cmd.linux;

import com.github.dakusui.actionunit.actions.cmd.Commander;
import com.github.dakusui.actionunit.actions.cmd.CommanderInitializer;

public class Cmd extends Commander<Cmd> {
  public Cmd(CommanderInitializer initializer) {
    super(initializer);
    initializer.init(this);
  }

  @Override
  public Cmd command(String command) {
    return super.command(command);
  }
}