package com.github.dakusui.actionunit.actions.cmd.linux;

import com.github.dakusui.actionunit.actions.cmd.Commander;
import com.github.dakusui.actionunit.core.context.ContextFunction;

import java.io.File;
import java.util.function.IntFunction;

import static java.util.Objects.requireNonNull;

public class Rm extends Commander<Rm> {
  public Rm(IntFunction<String> parameterPlaceHolderFormatter) {
    super(parameterPlaceHolderFormatter);
    this.command("rm");
  }

  public Rm recursive() {
    return this.addOption("-r");
  }

  public Rm force() {
    return this.addOption("-f");
  }

  public Rm file(File file) {
    return this.file(requireNonNull(file).getAbsolutePath());
  }

  public Rm file(String file) {
    return this.append(" ").append(file);
  }

  public Rm file(ContextFunction<String> file) {
    return this.append(" ").append(file);
  }
}
