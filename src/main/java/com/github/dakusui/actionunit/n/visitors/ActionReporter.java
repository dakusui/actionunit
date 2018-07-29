package com.github.dakusui.actionunit.n.visitors;

import com.github.dakusui.actionunit.n.core.Action;

import java.util.Map;

import static java.util.Objects.requireNonNull;

public class ActionReporter extends ActionPrinter {
  private final Map<Action, Record> report;

  public ActionReporter(Map<Action, Record> report) {
    this.report = requireNonNull(report);
  }

  public void report(Action action) {
    requireNonNull(action).accept(this);
  }

  @Override
  protected void handleAction(Action action) {
    System.out.println(String.format("%s[%s]%s", indent(), report.get(action), action));
  }
}
