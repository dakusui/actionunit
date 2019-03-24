package com.github.dakusui.actionunit.visitors;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.io.Writer;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class ReportingActionPerformer extends ActionPerformer {
  private final Map<Action, Record> report;

  private ReportingActionPerformer() {
    this(Context.create(), new LinkedHashMap<>());
  }

  private ReportingActionPerformer(Context context, Map<Action, Record> report) {
    super(context);
    this.report = report;
  }

  @Override
  protected Action.Visitor newInstance(Context context) {
    return new ReportingActionPerformer(context, this.report);
  }

  @Override
  protected void callAccept(Action action, Action.Visitor visitor) {
    synchronized (report) {
      report.computeIfAbsent(action, a -> createRecord());
    }
    Record record = requireNonNull(report.get(action));
    record.started();
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

  public void performAndReport(Action action, Writer writer) {
    try {
      perform(action);
    } finally {
      new ActionReporter(writer, this.getReport()).report(action);
    }
  }

  public Map<Action, Record> getReport() {
    return this.report;
  }

  protected Record createRecord() {
    return new Record();
  }

  public static ReportingActionPerformer create() {
    return new ReportingActionPerformer();
  }
}
