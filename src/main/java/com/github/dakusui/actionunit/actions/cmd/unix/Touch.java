package com.github.dakusui.actionunit.actions.cmd.unix;

import com.github.dakusui.actionunit.actions.cmd.Commander;
import com.github.dakusui.actionunit.actions.cmd.CommanderConfig;
import com.github.dakusui.actionunit.core.Context;

import java.io.File;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class Touch extends Commander<Touch> {
  public Touch(CommanderConfig initializer) {
    super(initializer, "touch");
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

  public Touch file(Function<Context, String> file) {
    return this.add(requireNonNull(file));
  }
}
