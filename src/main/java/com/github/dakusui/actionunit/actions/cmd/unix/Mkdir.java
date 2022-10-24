package com.github.dakusui.actionunit.actions.cmd.unix;

import com.github.dakusui.actionunit.actions.cmd.Commander;
import com.github.dakusui.actionunit.actions.cmd.CommanderInitializer;
import com.github.dakusui.actionunit.core.Context;

import java.io.File;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class Mkdir extends Commander<Mkdir> {
  public Mkdir(CommanderInitializer initializer) {
    super(initializer);
    initializer.init(this);
  }

  public Mkdir recursive() {
    return this.addOption("-p");
  }

  public Mkdir dir(String path) {
    return this.add(path);
  }

  public Mkdir dir(File path) {
    return this.dir(requireNonNull(path).getAbsolutePath());
  }

  public Mkdir dir(Function<Context, String> path) {
    return this.add(requireNonNull(path));
  }
}
