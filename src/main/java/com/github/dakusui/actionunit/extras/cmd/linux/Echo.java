package com.github.dakusui.actionunit.extras.cmd.linux;

import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.extras.cmd.Commander;
import com.github.dakusui.actionunit.extras.cmd.CommanderOption;

/**
 * <pre>
 * NAME
 *        echo - display a line of text
 *
 * SYNOPSIS
 *        echo [SHORT-OPTION]... [STRING]...
 *        echo LONG-OPTION
 * </pre>
 */
public class Echo extends Commander<Echo> {
  public Echo(Context context) {
    super(context);
  }

  @Override
  protected String commandPath() {
    return "/bin/echo";
  }

  /**
   * do not output the trailing newline.
   *
   * @return this object
   */
  public Echo noTrailingNewline() {
    return this.add(Option.NO_TRAILING_NEWLINE);
  }

  /**
   * enable interpretation of backslash escapes.
   *
   * @return this object
   */
  public Echo enableBackslashInterpretation() {
    return this.add(Option.ENABLE_BACKSLASH_INTERPRETATION);
  }

  /**
   * disable interpretation of backslash escapes (default).
   *
   * @return this object
   */
  public Echo disableBackslashInterpretation() {
    return this.add(Option.DISABLE_BACKSLASH_INTERPRETATION);
  }

  public enum Option implements CommanderOption {
    NO_TRAILING_NEWLINE("-n"),
    ENABLE_BACKSLASH_INTERPRETATION("-e"),
    DISABLE_BACKSLASH_INTERPRETATION("-E"),;

    private final String shortFormat;

    Option(String shortFormat) {
      this.shortFormat = shortFormat;
    }

    @Override
    public String longFormat() {
      return null;
    }

    @Override
    public String shortFormat() {
      return this.shortFormat;
    }
  }
}
