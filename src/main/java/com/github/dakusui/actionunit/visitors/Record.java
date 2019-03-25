package com.github.dakusui.actionunit.visitors;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Record implements Iterable<Record.Run> {

  private final List<Run> runs = Collections.synchronizedList(new LinkedList<>());

  public void started() {
  }

  public void succeeded() {
    runs.add(Run.SUCCEEDED);
  }

  public void failed(Throwable t) {
    runs.add(Run.failed(t));
  }

  @Override
  public Iterator<Run> iterator() {
    return runs.iterator();
  }

  @Override
  public String toString() {
    return formatRecord(this);
  }

  private static String formatRecord(Record runs) {
    StringBuilder b = new StringBuilder();
    if (runs != null)
      runs.forEach(run -> b.append(run.toString()));
    return summarize(b.toString());
  }

  private static String summarize(String in) {
    StringBuilder b = new StringBuilder();
    Matcher matcher = Pattern.compile("((.)\\2{0,})").matcher(in);
    int i = 0;
    while (matcher.find(i)) {
      String matched = matcher.group(1);
      if (matched.length() > 3) {
        b.append(matched.charAt(0));
        b.append("...");
      } else
        b.append(matched);
      i += matched.length();
    }
    return b.toString();
  }

  /**
   * {@code o} for Okay, {@code F} for fail, and {@code E} for error.
   * A fail means an assertion error, which is raised when a test fails.
   */
  public interface Run {
    Run SUCCEEDED = new Run() {
      public String toString() {
        return "o";
      }
    };

    static Run failed(Throwable t) {
      Objects.requireNonNull(t);
      return new Run() {
        @Override
        public String toString() {
          return t instanceof AssertionError
              ? "F"
              : "E";
        }
      };
    }
  }
}
