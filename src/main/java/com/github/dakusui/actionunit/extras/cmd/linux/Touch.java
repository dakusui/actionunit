package com.github.dakusui.actionunit.extras.cmd.linux;

import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.extras.cmd.Commander;

public class Touch extends Commander<Touch> {
  public Touch(Context context) {
    super(context);
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
