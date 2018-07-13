package com.github.dakusui.actionunit.extras.cmd.linux;

import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.extras.cmd.Commander;
import com.github.dakusui.actionunit.extras.cmd.CommanderOption;

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
public class Ls extends Commander<Ls> {

  public Ls(Context context) {
    super(context);
  }

  @Override
  protected String program() {
    return "/bin/ls";
  }

  public Ls longListing() {
    return add(Option.LONG_LISTING);
  }

  public Ls all() {
    return add(Option.ALL);
  }

  public Ls humanReadable() {
    return add(Option.HUMAN_READABLE);
  }

  public Ls classify() {
    return add(Option.CLASSIFY);
  }

  public Ls reverse() {
    return add(Option.REVERSE);
  }

  public Ls sortByMtime() {
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