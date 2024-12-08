package com.github.dakusui.actionunit.visitors;

import com.github.dakusui.actionunit.actions.Composite;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.io.Writer;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * A basic action reporter which writes an action tree and its report to given writers
 *
 * @see ReportingActionPerformer
 */
public class ActionReporter extends ActionPrinter {
  public static final Predicate<Action>   DEFAULT_CONDITION_TO_SQUASH_ACTION = v -> v instanceof Composite;
  private final       List<Boolean>       failingContext                     = new LinkedList<>();
  private final       Predicate<Action>   conditionToSquashAction;
  private             int                 emptyLevel                         = 0;
  private             int                 depth                              = 0;
  private final       Map<Action, Record> report;
  private final       Writer              warnWriter;
  private final       Writer              traceWriter;
  private final       Writer              debugWriter;
  private final       Writer              infoWriter;
  private final       int                 forcePrintLevelForUnexercisedActions;
  String previousIndent = "";

  /**
   * Creates an object of this class.
   * This reporter implements a functionality to squash actions to make output concise.
   * It is controlled by a predicate given through `conditionToSquashAction` parameter.
   * Also, depending on whether an action is exercised or not, a writer used to print it out will be chosen by internal logic of this class.
   * Furthermore, actions in a deep level of a tree will be considered "unexercised" forcibly.
   * The threshold level can be controlled by the parameter `forcePrintLevelForUnexercisedActions`.
   *
   * @param conditionToSquashAction              A condition to "squash" a record of an action to make report concise.
   * @param warnWriter                           A writer used for "warn" level.
   * @param infoWriter                           A writer used for "info" level.
   * @param debugWriter                          A writer used for "debug" level.
   * @param traceWriter                          A writer used for "trace" level.
   * @param report                               A map that stores an action tree's result.
   * @param forcePrintLevelForUnexercisedActions A level under which action should be considered to be "unexercised".
   */
  public ActionReporter(Predicate<Action> conditionToSquashAction, Writer warnWriter, Writer infoWriter, Writer debugWriter, Writer traceWriter, Map<Action, Record> report, int forcePrintLevelForUnexercisedActions) {
    super(infoWriter);
    this.conditionToSquashAction = conditionToSquashAction;
    this.report = requireNonNull(report);
    this.warnWriter = requireNonNull(warnWriter);
    this.debugWriter = requireNonNull(debugWriter);
    this.infoWriter = infoWriter;
    this.traceWriter = traceWriter;
    this.forcePrintLevelForUnexercisedActions = forcePrintLevelForUnexercisedActions;
  }

  /**
   * Creates an action of this class.
   *
   * @param writer A writer through which a report is written.
   * @param report A report data that records the result of actions in the tree.
   */
  public ActionReporter(Writer writer, Map<Action, Record> report) {
    this(DEFAULT_CONDITION_TO_SQUASH_ACTION, writer, writer, writer, writer, report, 2);
  }

  /**
   * An entry-point to start reporting.
   *
   * @param action An action from which reporting starts.
   */
  public void report(Action action) {
    requireNonNull(action).accept(this);
  }

  @Override
  protected void handleAction(Action action) {
    if (this.conditionToSquashAction.test(action)) {
      this.previousIndent = indent();
      return;
    }
    Record runs = report.get(action);
    String message = format("%s[%s]%s", indent(), runs != null ? runs : "", action);
    this.previousIndent = "";
    if (isInFailingContext()) {
      this.warnWriter.writeLine(message);
    } else {
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

  /**
   * //@formatter.off
   * ----
   * > [E:0]for each of (noname) parallely
   * >  +-[EE:0]print1
   * >    |   [EE:0](noname)
   * >    +-[]print2
   * >      |   [](noname)
   * >      +-[]print2-1
   * >      |   [](noname)
   * >      +-[]print2-2
   * >        [](noname)
   * ----
   * //@format:on
   */
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
    return mergeStrings(this.previousIndent, b.toString());
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

  boolean isInFailingContext() {
    return !this.failingContext.isEmpty() && this.failingContext.get(0);
  }

  void pushFailingContext(boolean newContext) {
    failingContext.add(0, newContext);
  }

  void popFailingContext() {
    failingContext.remove(0);
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

  private static Action nextOf(Action each, List<? extends Action> path) {
    return path.get(path.indexOf(each) + 1);
  }

  /**
   * Merges two string into one.
   * A white space in `a` or `b` will be replaced with non-white space in the other at the same position.
   * In case both have non-white space in the same position, the latter's (`b`) overrides the first's (`a`).
   * <p>
   * .Example input
   * ----
   * a:"hello    "
   * b:"    O WORLD "
   * ----
   * <p>
   * .Example output
   * ----
   * "hellO WORLD "
   * ----
   *
   * @param a A string to be merged.
   * @param b A stringto be merged
   * @return The merged result string.
   */
  private static String mergeStrings(String a, String b) {
    StringBuilder builder = new StringBuilder();
    int min = Math.min(a.length(), b.length());
    for (int i = 0; i < min; i++) {
      char ach = a.charAt(i);
      char bch = b.charAt(i);
      if (bch != ' ')
        builder.append(bch);
      else
        builder.append(ach);
    }
    // Whichever longer, the result is the same since the shorter.substring(min) will become an empty
    // string.
    builder.append(a.substring(min));
    builder.append(b.substring(min));
    return builder.toString();
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
}
