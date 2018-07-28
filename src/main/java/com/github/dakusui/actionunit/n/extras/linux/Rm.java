package com.github.dakusui.actionunit.n.extras.linux;

import com.github.dakusui.actionunit.n.actions.cmd.Commander;
import com.github.dakusui.actionunit.n.core.Context;

import java.io.File;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class Rm extends Commander<Rm> {
  public Rm() {
    super();
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

  public Rm file(Function<Context, String> file) {
    return this.addq(file);
  }

  @Override
  protected String program() {
    return "/bin/rm";
  }
}
