package com.github.dakusui.actionunit.linux;

import com.github.dakusui.actionunit.actions.cmd.CompatCommander;
import com.github.dakusui.actionunit.actions.cmd.CommanderOption;
import com.github.dakusui.actionunit.core.Context;

import java.util.function.Function;

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
public class Echo extends CompatCommander<Echo> {
  /**
   * Creates an object of this class.
   */
  public Echo() {
    super();
  }

  @Override
  protected String program() {
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
   * Adds a message to this object. The message will be escaped so that it can be
   * placed inside single quotes automatically.
   * <p>
   * In case this automatic escaping is not preferable, use {@code Commander#add(String)}
   * method.
   *
   * @param message A message to be added.
   * @return this object
   * @see CompatCommander#add(String)
   * @see CompatCommander#addq(String)
   */
  public Echo message(Function<Context, String> message) {
    return this.addq(message);
  }

  public Echo message(String message) {
    return this.addq(message);
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
