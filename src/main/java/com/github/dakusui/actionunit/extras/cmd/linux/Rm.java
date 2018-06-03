package com.github.dakusui.actionunit.extras.cmd.linux;

import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.extras.cmd.Commander;

import java.io.File;

import static java.util.Objects.requireNonNull;

public class Rm extends Commander<Rm> {
  public Rm(Context context) {
    super(context);
  }

  public Rm recursive() {
    return this.add("-r");
  }

  public Rm force() {
    return this.add("-f");
  }

  public Rm file(File file) {
    return this.file(requireNonNull(file).getAbsolutePath());
  }

  public Rm file(String file) {
    return this.addq(file);
  }

  @Override
  protected String commandPath() {
    return "/bin/rm";
  }
}
