package com.github.dakusui.actionunit.n.visitors;

import com.github.dakusui.actionunit.n.core.Action;
import com.github.dakusui.actionunit.n.core.Context;

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

  public ActionReporter perform(Action action) {
    callAccept(requireNonNull(action), this);
    return new ActionReporter(this.report);
  }

  public static ReportingActionPerformer create() {
    return new ReportingActionPerformer();
  }
}
