package com.github.dakusui.actionunit.actions.cmd.linux;

import com.github.dakusui.actionunit.actions.cmd.Commander;
import com.github.dakusui.actionunit.actions.cmd.CommanderInitializer;
import com.github.dakusui.actionunit.core.context.ContextFunction;

import java.io.File;

import static java.util.Objects.requireNonNull;

public class Touch extends Commander<Touch> {
  public Touch(CommanderInitializer initializer) {
    super(initializer);
    initializer.init(this);
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
