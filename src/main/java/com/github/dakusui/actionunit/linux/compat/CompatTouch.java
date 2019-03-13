package com.github.dakusui.actionunit.linux.compat;

import com.github.dakusui.actionunit.actions.cmd.compat.CompatCommander;

public class CompatTouch extends CompatCommander<CompatTouch> {
  public CompatTouch() {
    super();
  }

  public CompatTouch noCreate() {
    return this.add("-c");
  }

  public CompatTouch file(String file) {
    return this.addq(file);
  }

  @Override
  protected String program() {
    return "/usr/bin/touch";
  }
}
