package com.github.dakusui.actionunit.actions.cmd.unix;

import com.github.dakusui.actionunit.actions.cmd.Commander;
import com.github.dakusui.actionunit.actions.cmd.CommanderInitializer;
import com.github.dakusui.actionunit.core.context.ContextFunction;

import java.io.File;

import static java.util.Objects.requireNonNull;

public class Rm extends Commander<Rm> {
  public Rm(CommanderInitializer initializer) {
    super(initializer);
    initializer.init(this);
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
    return this.add(file);
  }

  public Rm file(ContextFunction<String> file) {
    return this.add(file);
  }
}
