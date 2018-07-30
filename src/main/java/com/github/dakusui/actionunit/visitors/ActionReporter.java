package com.github.dakusui.actionunit.visitors;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.io.Writer;

import java.util.Map;

import static java.util.Objects.requireNonNull;

public class ActionReporter extends ActionPrinter {
  private final Map<Action, Record> report;

  public ActionReporter(Writer writer, Map<Action, Record> report) {
    super(writer);
    this.report = requireNonNull(report);
  }

  public void report(Action action) {
    requireNonNull(action).accept(this);
  }

  @Override
  protected void handleAction(Action action) {
    this.writer.writeLine(String.format("%s[%s]%s", indent(), report.get(action), action));
  }
}
