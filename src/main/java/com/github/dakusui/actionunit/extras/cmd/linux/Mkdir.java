package com.github.dakusui.actionunit.extras.cmd.linux;

import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.extras.cmd.Commander;

import java.io.File;

public class Mkdir extends Commander<Mkdir> {
  public Mkdir(Context context) {
    super(context);
  }

  @Override
  protected String program() {
    return "/bin/mkdir";
  }

  public Mkdir recursive() {
    return this.add("-p");
  }

  public Mkdir dir(String path) {
    return dir(new File(path));
  }

  public Mkdir dir(File path) {
    return this.add((path.isAbsolute() ?
        path.getAbsolutePath() :
        new File(this.cwd(), path.getPath()).getAbsolutePath()
    ));
  }
}
