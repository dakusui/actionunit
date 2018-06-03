package com.github.dakusui.actionunit.extras.cmd.linux;

import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.extras.cmd.Commander;
import com.github.dakusui.actionunit.extras.cmd.CommanderOption;

import java.util.function.Supplier;

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
  /**
   * Creates an object of this class.
   *
   * @param context A context from which an action is created by this object.
   */
  public Echo(Context context) {
    super(context);
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
   * @see Commander#add(String)
   * @see Commander#addq(String)
   */
  public Echo message(Supplier<String> message) {
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
