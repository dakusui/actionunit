package com.github.dakusui.actionunit.extras.cmd;

import com.github.dakusui.actionunit.helpers.Checks;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

enum CommanderUtils {
  ;

  static String summarize(String commandLine, int length) {
    Checks.requireArgument(l -> l > 3, length);
    return requireNonNull(commandLine).length() < length ?
        replaceNewLines(commandLine) :
        replaceNewLines(commandLine).substring(0, length - 3) + "...";
  }

  static String quoteWithSingleQuotesForShell(String s) {
    return String.format("'%s'", escapeSingleQuotesForShell(s));
  }


  static String toString(Object obj) {
    return obj == null ?
        "null" :
        isLambda(obj) ?
            "(lambda)" :
            Objects.toString(obj);
  }

  /*
   * This is a dirty trick to determine is a given object is a lambda or not.
   */
  private static boolean isLambda(Object obj) {
    return obj.getClass().toString().contains("$$Lambda$");
  }


  private static String replaceNewLines(String s) {
    return s.replaceAll("\n", " ");
  }

  private static String escapeSingleQuotesForShell(String s) {
    return requireNonNull(s).replaceAll("('+)", "'\"$1\"'");
  }
}
