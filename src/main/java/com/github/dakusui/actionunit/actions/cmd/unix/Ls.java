package com.github.dakusui.actionunit.actions.cmd.unix;

import com.github.dakusui.actionunit.actions.cmd.Commander;
import com.github.dakusui.actionunit.actions.cmd.CommanderConfig;
import com.github.dakusui.actionunit.core.context.ContextFunction;

import java.io.File;

import static java.util.Objects.requireNonNull;

public class Ls extends Commander<Ls> {
  public Ls(CommanderConfig config) {
    super(config, "ls");
  }

  public Ls longListing() {
    return addOption("-l");
  }

  public Ls all() {
    return addOption("-a");
  }

  public Ls size() {
    return addOption("-s");
  }

  public Ls humanReadable() {
    return addOption("-h");
  }

  /**
   * Add an indicator to each entry.
   *
   * @return This object
   */
  public Ls classify() {
    return addOption("-F");
  }

  public Ls reverse() {
    return addOption("-r");
  }

  public Ls sortByMtime() {
    return addOption("-t");
  }

  public Ls file(ContextFunction<String> path) {
    return this.add(path);
  }

  public Ls file(String path) {
    return this.add(path);
  }

  public Ls file(File path) {
    return this.file(requireNonNull(path).getAbsolutePath());
  }
}
