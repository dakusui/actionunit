package com.github.dakusui.actionunit.actions.cmd.linux;

import com.github.dakusui.actionunit.actions.cmd.Commander;

import java.io.File;
import java.util.function.IntFunction;

import static java.util.Objects.requireNonNull;

public class Touch extends Commander<Touch> {
  public Touch(IntFunction<String> parameterPlaceHolderFormatter) {
    super(parameterPlaceHolderFormatter);
    this.command("touch");
  }

  public Touch noCreate() {
    return this.addOption("-c");
  }

  public Touch file(String file) {
    return this.append(" ").appendq(requireNonNull(file));
  }

  public Touch file(File file) {
    return this.file(requireNonNull(file).getAbsolutePath());
  }
}
