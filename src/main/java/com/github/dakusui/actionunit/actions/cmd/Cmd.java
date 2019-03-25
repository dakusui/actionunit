package com.github.dakusui.actionunit.actions.cmd;

import java.util.function.IntFunction;

public class Cmd extends Commander<Cmd> {
  public Cmd(IntFunction<String> parameterPlaceHolderFormatter) {
    super(parameterPlaceHolderFormatter);
  }

  @Override
  public Cmd command(String command) {
    return super.command(command);
  }
}