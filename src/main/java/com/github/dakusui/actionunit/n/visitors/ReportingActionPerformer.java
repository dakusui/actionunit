package com.github.dakusui.actionunit.n.visitors;

import com.github.dakusui.actionunit.n.core.Action;
import com.github.dakusui.actionunit.n.core.Context;
import com.github.dakusui.actionunit.n.io.Writer;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class ReportingActionPerformer extends ActionPerformer {
  private final Map<Action, Record> report;
  private final Writer              writer;

  private ReportingActionPerformer(Writer writer) {
    this(writer, Context.create(), new LinkedHashMap<>());
  }

  private ReportingActionPerformer(Writer writer, Context context, Map<Action, Record> report) {
    super(context);
    this.report = report;
    this.writer = writer;
  }

  @Override
  protected Action.Visitor newInstance(Context context) {
    return new ReportingActionPerformer(this.writer, context, this.report);
  }

  @Override
  protected void callAccept(Action action, Action.Visitor visitor) {
    synchronized (report) {
      report.computeIfAbsent(action, a -> new Record());
    }
    Record record = requireNonNull(report.get(action));
    try {
      action.accept(visitor);
      record.succeeded();
    } catch (Throwable t) {
      record.failed(t);
      throw t;
    }
  }

  public void perform(Action action) {
    callAccept(requireNonNull(action), this);
  }

  public void performAndReport(Action action) {
    try {
      perform(action);
    } finally {
      new ActionReporter(this.writer, this.report).report(action);
    }
  }

  public static ReportingActionPerformer create(Writer writer) {
    return new ReportingActionPerformer(writer);
  }
}
