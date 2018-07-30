package com.github.dakusui.actionunit.linux;

import com.github.dakusui.actionunit.actions.cmd.Commander;

public class Touch extends Commander<Touch> {
  public Touch() {
    super();
  }

  public Touch noCreate() {
    return this.add("-c");
  }

  public Touch file(String file) {
    return this.addq(file);
  }

  @Override
  protected String program() {
    return "/usr/bin/touch";
  }
}
