package com.github.dakusui.actionunit.linux;

import com.github.dakusui.actionunit.actions.cmd.CommandLineComposer;
import com.github.dakusui.actionunit.actions.cmd.Commander;
import com.github.dakusui.actionunit.core.Context;

import java.io.File;

import static java.util.Objects.requireNonNull;

public class Cat extends Commander<Cat> {
  public Cat(Context context) {
    super();
  }

  public Cat number() {
//    return this.add("-n");
    return this;
  }

  public Cat file(String fileName) {
    //return this.add(requireNonNull(fileName));
    return this;
  }

  public Cat file(File file) {
    //return this.add(requireNonNull(file).getAbsolutePath());
    return this;
  }

  public Cat file() {
    return this;
  }

  @Override
  protected CommandLineComposer commandLineComposer() {
    return new CommandLineComposer() {
      @Override
      public String commandLineString() {
        return "/bin/cat";
      }
    };
  }

  @Override
  protected String[] variableNames() {
    return new String[0];
  }
}
