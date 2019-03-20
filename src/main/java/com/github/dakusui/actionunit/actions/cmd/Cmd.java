package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.actionunit.core.ActionSupport;
import com.github.dakusui.actionunit.core.context.StreamGenerator;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import org.junit.Test;

import java.util.function.IntFunction;

public class Cmd extends Commander<Cmd> {
  public Cmd(IntFunction<String> parameterPlaceHolderFomatter) {
    super(parameterPlaceHolderFomatter);
  }

  @Override
  public Cmd command(String command, String... variableNames) {
    return super.command(command, variableNames);
  }

  @Override
  public Cmd knownVariables(String... variableNames) {
    return super.knownVariables(variableNames);
  }

  @Override
  public Cmd append(String text) {
    return super.append(text);
  }

  @Override
  public Cmd appendVariable(String variableName) {
    return super.appendVariable(variableName);
  }

}