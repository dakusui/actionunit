package com.github.dakusui.actionunit.linux.compat;

import com.github.dakusui.actionunit.actions.cmd.compat.CompatCommander;

import java.io.File;

public class CompatMkdir extends CompatCommander<CompatMkdir> {
  public CompatMkdir() {
    super();
  }

  @Override
  protected String program() {
    return "/bin/mkdir";
  }

  public CompatMkdir recursive() {
    return this.add("-p");
  }

  public CompatMkdir dir(String path) {
    return dir(new File(path));
  }

  public CompatMkdir dir(File path) {
    return this.add((path.isAbsolute() ?
        path.getAbsolutePath() :
        new File(this.cwd(), path.getPath()).getAbsolutePath()
    ));
  }
}
