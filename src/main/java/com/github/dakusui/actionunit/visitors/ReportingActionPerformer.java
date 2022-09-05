package com.github.dakusui.actionunit.visitors;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.io.Writer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class ReportingActionPerformer extends ActionPerformer {
  private static final Logger              LOGGER = LoggerFactory.getLogger(ReportingActionPerformer.class);
  private final        Map<Action, Record> report;

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
    Record record;
    synchronized (report) {
      record = report.computeIfAbsent(action, a -> createRecord());
    }
    if (record == null) {
      LOGGER.error("record became null for action:{}({})", action, action.getClass());
      assert false;
    }
    long timeStartedInMillis = record.started();
    try {
      action.accept(visitor);
      record.succeeded(System.currentTimeMillis() - timeStartedInMillis);
    } catch (Throwable t) {
      record.failed(System.currentTimeMillis() - timeStartedInMillis, t);
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
