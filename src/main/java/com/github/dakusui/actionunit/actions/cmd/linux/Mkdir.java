package com.github.dakusui.actionunit.actions.cmd.linux;

import com.github.dakusui.actionunit.actions.cmd.Commander;
import com.github.dakusui.actionunit.core.context.ContextFunction;
import com.github.dakusui.actionunit.core.context.ContextFunctions;

import java.io.File;
import java.util.function.IntFunction;

import static java.util.Objects.requireNonNull;

public class Mkdir extends Commander<Mkdir> {
  public Mkdir(IntFunction<String> parameterPlaceHolderFormatter) {
    super(parameterPlaceHolderFormatter);
    this.command("mkdir");
  }

  public Mkdir recursive() {
    return this.addOption("-p");
  }

  public Mkdir dir(String path) {
    return this.append(" ").appendq(requireNonNull(path));
  }

  public Mkdir dir(File path) {
    return this.dir(requireNonNull(path).getAbsolutePath());
  }

  public Mkdir dir(ContextFunction<String> path) {
    return this.append(" ").appendq(requireNonNull(path));
  }
}
