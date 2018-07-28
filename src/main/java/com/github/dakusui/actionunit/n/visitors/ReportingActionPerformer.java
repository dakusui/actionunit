package com.github.dakusui.actionunit.n.visitors;

import com.github.dakusui.actionunit.n.exceptions.ActionException;
import com.github.dakusui.actionunit.n.core.Action;
import com.github.dakusui.actionunit.n.core.Context;
import com.github.dakusui.actionunit.visitors.reporting.Report;

import java.util.LinkedHashMap;
import java.util.Map;

public class ReportingActionPerformer extends ActionPerformer {
  private Map<Action, Report.Record> report;

  private ReportingActionPerformer() {
    this(null, new LinkedHashMap<>());
  }

  private ReportingActionPerformer(Context context, Map<Action, Report.Record> report) {
    super(context);
    this.report = report;
  }

  @Override
  protected Action.Visitor newInstance(Context context) {
    return new ReportingActionPerformer(context, this.report);
  }

  @Override
  protected void callAccept(Action action, Action.Visitor visitor) {
    report.computeIfAbsent(action, a -> new Report.Record());

    Report.Record record = report.get(action);
    try {
      action.accept(visitor);
      record.succeeded();
    } catch (Throwable t) {
      record.failed(t);
      throw ActionException.wrap(t);
    }
  }

  public static ReportingActionPerformer create() {
    return new ReportingActionPerformer();
  }
}
