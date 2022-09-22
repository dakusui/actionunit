package com.github.dakusui.actionunit.visitors;

import com.github.dakusui.actionunit.actions.Composite;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.io.Writer;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class ActionReporter extends ActionPrinter {
  private final List<Boolean>       failingContext = new LinkedList<>();
  private       int                 emptyLevel     = 0;
  private       int                 depth          = 0;
  private final Map<Action, Record> report;
  private final Writer              warnWriter;
  private final Writer              traceWriter;
  private final Writer              debugWriter;
  private final Writer              infoWriter;
  private final int                 forcePrintLevelForUnexercisedActions;

  public ActionReporter(Writer warnWriter, Writer infoWriter, Writer debugWriter, Writer traceWriter, Map<Action, Record> report, int forcePrintLevelForUnexercisedActions) {
    super(infoWriter);
    this.report = requireNonNull(report);
    this.warnWriter = requireNonNull(warnWriter);
    this.debugWriter = requireNonNull(debugWriter);
    this.infoWriter = infoWriter;
    this.traceWriter = traceWriter;
    this.forcePrintLevelForUnexercisedActions = forcePrintLevelForUnexercisedActions;
  }

  public ActionReporter(Writer writer, Map<Action, Record> report) {
    this(writer, writer, writer, writer, report, 2);
  }

  public void report(Action action) {
    requireNonNull(action).accept(this);
  }

  @Override
  protected void handleAction(Action action) {
    Record runs = report.get(action);
    String message = format("%s[%s]%s", indent(), runs != null ? runs : "", action);
    if (isInFailingContext()) {
      this.warnWriter.writeLine(message);
    } else {
      if (action instanceof Composite)
        return;
      if (emptyLevel < 1) { // Top level unexercised + exercised ones
        if (passingLevels() < 1) {
          this.infoWriter.writeLine(message);
        } else {
          writeLineForUnexercisedAction(message);
        }
      } else {
        writeLineForUnexercisedAction(message);
      }
    }
  }

  @Override
  public String indent() {
    List<? extends Action> path = this.path();
    StringBuilder b = new StringBuilder();
    if (!path.isEmpty()) {
      Action last = path.get(path.size() - 1);
      for (Action each : path) {
        if (each instanceof Composite) {
          if (each == last) {
            if (((Composite) each).isParallel())
              b.append("*-");
            else
              b.append("+-");
          } else {
            if (isLastChild(nextOf(each, path), each))
              b.append("  ");
            else
              b.append("| ");
          }
        } else {
          b.append("  ");
        }
      }
    }
    return b.toString();
  }

  private static Action nextOf(Action each, List<? extends Action> path) {
    return path.get(path.indexOf(each) + 1);
  }

  private static boolean isFirstChild(Action each, Action parent) {
    if (parent instanceof Composite) {
      return ((Composite) parent).children().indexOf(each) == 0;
    }
    return true;
  }

  private static boolean isLastChild(Action each, Action parent) {
    if (parent instanceof Composite) {
      int index = ((Composite) parent).children().indexOf(each);
      int size = ((Composite) parent).children().size();
      assert index >= 0;
      return index == size - 1;
    }
    return true;
  }

  private void writeLineForUnexercisedAction(String message) {
    // unexercised
    if (depth < this.forcePrintLevelForUnexercisedActions)
      this.debugWriter.writeLine(message);
    else
      this.traceWriter.writeLine(message);
  }

  private int passingLevels() {
    int ret = 0;
    for (boolean each : this.failingContext)
      if (!each)
        ret++;
    return ret;
  }

  boolean isInFailingContext() {
    return !this.failingContext.isEmpty() && this.failingContext.get(0);
  }

  void pushFailingContext(boolean newContext) {
    failingContext.add(0, newContext);
  }

  void popFailingContext() {
    failingContext.remove(0);
  }

  @Override
  protected void enter(Action action) {
    super.enter(action);
    depth++;
    Record runs = report.get(action);
    pushFailingContext(runs != null && runs.allFailing());
    if (runs == null)
      emptyLevel++;

  }

  @Override
  protected void leave(Action action) {
    Record runs = report.get(action);
    if (runs == null)
      emptyLevel--;
    popFailingContext();
    depth--;
    super.leave(action);
  }
  /*
  [E:0]for each of (noname) parallely
  [EE:0]do sequentially
  |  [EE:0]print
  |    [EE:0](noname)
  | []print
  |    [](noname)
  | :[]print
    :   [](noname)
    :[]print
    :   [](noname)

        : []parallel1
        : | | []sequential(1.1)
        : | | []sequential(1.1)
        : | []sequential(2)
        : |[]sequential(1)
        : []parallel2

   */
}
