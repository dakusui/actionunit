package com.github.dakusui.actionunit.linux.compat;

import com.github.dakusui.actionunit.actions.cmd.compat.CompatCommander;
import com.github.dakusui.actionunit.core.Context;

import java.io.File;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class CompatRm extends CompatCommander<CompatRm> {
  public CompatRm() {
    super();
  }

  public CompatRm recursive() {
    return this.add("-r");
  }

  public CompatRm force() {
    return this.add("-f");
  }

  public CompatRm file(File file) {
    return this.file(requireNonNull(file).getAbsolutePath());
  }

  public CompatRm file(String file) {
    return this.addq(file);
  }

  public CompatRm file(Function<Context, String> file) {
    return this.addq(file);
  }

  @Override
  protected String program() {
    return "/bin/rm";
  }
}
