package com.github.dakusui.actionunit.linux.compat;

import com.github.dakusui.actionunit.actions.cmd.compat.CompatCommander;
import com.github.dakusui.actionunit.core.Context;

import java.io.File;

import static java.util.Objects.requireNonNull;

public class CompatCat extends CompatCommander<CompatCat> {
  public CompatCat(Context context) {
    super();
  }

  public CompatCat number() {
    return this.add("-n");
  }

  public CompatCat file(String fileName) {
    return this.add(requireNonNull(fileName));
  }

  public CompatCat file(File file) {
    return this.add(requireNonNull(file).getAbsolutePath());
  }

  public CompatCat file() {
    return this;
  }

  @Override
  protected String program() {
    return "/bin/cat";
  }
}
