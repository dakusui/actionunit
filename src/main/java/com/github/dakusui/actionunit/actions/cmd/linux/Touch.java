package com.github.dakusui.actionunit.actions.cmd.linux;

import com.github.dakusui.actionunit.actions.cmd.Commander;
import com.github.dakusui.actionunit.core.context.ContextFunction;

import java.io.File;
import java.util.function.Function;
import java.util.function.IntFunction;

import static java.util.Objects.requireNonNull;

public class Touch extends Commander<Touch> {
  public Touch(Function<String[], IntFunction<String>> parameterPlaceHolderFormatter) {
    super(parameterPlaceHolderFormatter);
    this.command("touch");
  }

  public Touch noCreate() {
    return this.addOption("-c");
  }

  public Touch file(String file) {
    return this.add(file);
  }

  public Touch file(File file) {
    return this.file(requireNonNull(file).getAbsolutePath());
  }

  public Touch file(ContextFunction<String> file) {
    return this.add(requireNonNull(file));
  }
}
