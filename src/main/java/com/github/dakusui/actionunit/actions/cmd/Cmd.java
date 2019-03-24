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

  @Override
  public Cmd append(String text) {
    return super.append(text);
  }

  @Override
  public Cmd appendq(String text) {
    return super.appendq(text);
  }

  @Override
  public Cmd appendVariable(String variableName) {
    return super.appendVariable(variableName);
  }

  @Override
  public Cmd appendQuotedVariable(String variableName) {
    return super.appendQuotedVariable(variableName);
  }

  @Override
  public Cmd declareVariable(String variableName) {
    return super.declareVariable(variableName);
  }
}