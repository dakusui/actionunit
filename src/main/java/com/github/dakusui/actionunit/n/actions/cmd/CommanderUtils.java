package com.github.dakusui.actionunit.n.actions.cmd;

import com.github.dakusui.actionunit.n.utils.Checks;

import static java.util.Objects.requireNonNull;

public enum CommanderUtils {
  ;

  public static String summarize(String commandLine, int length) {
    Checks.requireArgument(l -> l > 3, length);
    return requireNonNull(commandLine).length() < length ?
        replaceNewLines(commandLine) :
        replaceNewLines(commandLine).substring(0, length - 3) + "...";
  }

  public static String quoteWithSingleQuotesForShell(String s) {
    return String.format("'%s'", escapeSingleQuotesForShell(s));
  }


  private static String replaceNewLines(String s) {
    return s.replaceAll("\n", " ");
  }

  private static String escapeSingleQuotesForShell(String s) {
    return requireNonNull(s).replaceAll("('+)", "'\"$1\"'");
  }
}
