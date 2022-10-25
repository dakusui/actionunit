package com.github.dakusui.actionunit.actions.cmd.unix;

import com.github.dakusui.actionunit.actions.cmd.Commander;
import com.github.dakusui.actionunit.actions.cmd.CommanderConfig;

/**
 * A simple action builder class for actions that run command line program.
 */
public class Cmd extends Commander<Cmd> {
  public Cmd(CommanderConfig config) {
    super(config);
  }
}