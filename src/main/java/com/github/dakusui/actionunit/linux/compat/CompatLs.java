package com.github.dakusui.actionunit.linux.compat;

import com.github.dakusui.actionunit.actions.cmd.compat.CommanderOption;
import com.github.dakusui.actionunit.actions.cmd.compat.CompatCommander;

import static java.util.Objects.requireNonNull;

/**
 * <pre>
 *   NAME
 *        ls - list directory contents
 *
 * SYNOPSIS
 *        ls [OPTION]... [FILE]...
 *
 * DESCRIPTION
 *        List  information  about  the  FILEs (the current directory by default).  Sort entries alphabetically if none of -cftuvSUX nor
 *        --sort is specified.
 *
 *        Mandatory arguments to long options are mandatory for short options too.
 * </pre>
 */
public class CompatLs extends CompatCommander<CompatLs> {

  public CompatLs() {
    super();
  }

  @Override
  protected String program() {
    return "/bin/ls";
  }

  public CompatLs longListing() {
    return add(Option.LONG_LISTING);
  }

  public CompatLs all() {
    return add(Option.ALL);
  }

  public CompatLs humanReadable() {
    return add(Option.HUMAN_READABLE);
  }

  public CompatLs classify() {
    return add(Option.CLASSIFY);
  }

  public CompatLs reverse() {
    return add(Option.REVERSE);
  }

  public CompatLs sortByMtime() {
    return add(Option.SORT_BY_MTIME);
  }

  /**
   * Options supported by this builder.
   */
  public enum Option implements CommanderOption {
    /**
     * use a long listing format.
     */
    LONG_LISTING("-l", null),
    /**
     * do not ignore entries starting with.
     */
    ALL("-a", "--all"),
    /**
     * with -l and/or -s, print human readable sizes
     * (e.g., 1K 234M 2G)
     */
    HUMAN_READABLE("-h", "--human-readable"),
    /**
     * append indicator to entries
     */
    CLASSIFY("-F", "--classify"),
    /**
     * reverse order while sorting.
     */
    REVERSE("-r", "--reverse"),
    /**
     * sort by modification time, newest first.
     */
    SORT_BY_MTIME("-t", null),;

    private final String shortFormat;
    private final String longFormat;

    Option(String shortFormat, String longFormat) {
      this.shortFormat = shortFormat;
      this.longFormat = longFormat;
    }

    @Override
    public String longFormat() {
      return requireNonNull(this.longFormat);
    }

    @Override
    public String shortFormat() {
      return this.shortFormat;
    }
  }
}