package com.github.dakusui.actionunit.actions.cmd.unix;

import com.github.dakusui.actionunit.actions.cmd.Commander;
import com.github.dakusui.actionunit.actions.cmd.CommanderConfig;
import com.github.dakusui.actionunit.core.Context;

import java.io.File;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class Rm extends Commander<Rm> {
  public Rm(CommanderConfig initializer) {
    super(initializer);
    initializer.setCommandNameFor(this);
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

  public Rm file(Function<Context, String> file) {
    return this.add(file);
  }
}
