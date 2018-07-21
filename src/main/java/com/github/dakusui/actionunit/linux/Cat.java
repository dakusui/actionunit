package com.github.dakusui.actionunit.linux;

import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.actions.cmd.Commander;

import java.io.File;

import static java.util.Objects.requireNonNull;

public class Cat extends Commander<Cat> {
  public Cat(Context context) {
    super();
  }

  public Cat number() {
    return this.add("-n");
  }

  public Cat file(String fileName) {
    return this.add(requireNonNull(fileName));
  }

  public Cat file(File file) {
    return this.add(requireNonNull(file).getAbsolutePath());
  }

  @Override
  protected String program() {
    return "/bin/cat";
  }
}
