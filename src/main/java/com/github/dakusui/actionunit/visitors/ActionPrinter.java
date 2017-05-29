package com.github.dakusui.actionunit.visitors;

import com.github.dakusui.actionunit.actions.*;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.helpers.Utils;

import java.util.function.Function;

import static com.github.dakusui.actionunit.helpers.Checks.checkNotNull;
import static com.github.dakusui.actionunit.helpers.Checks.notPrintable;
import static com.github.dakusui.actionunit.helpers.Utils.describe;

public abstract class ActionPrinter extends Action.Visitor.Base {
  /*
   * A writer through which this object's output is printed.
   */
  protected final ReportingActionRunner.Writer writer;
  /*
   * current indent level.
   */
  @SuppressWarnings("WeakerAccess")
  protected       int                          indent;

  public ActionPrinter(ReportingActionRunner.Writer writer) {
    this.writer = checkNotNull(writer);
  }

  protected void enter(Action action) {
    indent++;
  }

  protected void leave(Action action) {
    indent--;
  }

  /**
   * An extension point to customize format to print out each {@link Action} object.
   *
   * @param action an {@code Action} to be printed.
   */
  protected String describeAction(Action action) {
    return describe(action);
  }

  /**
   * An extension point to customize format a line to be printed by this object.
   *
   * @param s A line to be printed. Typically a description of an action.
   */
  protected void writeLine(String s) {
    boolean first = true;
    for (String each : s.split("\\n")) {
      this.writer.writeLine(Utils.spaces((this.indent + (first ? 0 : 1)) * 2) + each);
      first = false;
    }
  }

  public interface Factory extends Function<ReportingActionRunner.Writer, ActionPrinter> {
    Factory DEFAULT_INSTANCE = Impl::new;

    default ActionPrinter create(ReportingActionRunner.Writer writer) {
      return this.apply(writer);
    }

    default ActionPrinter create() {
      return this.apply(new ReportingActionRunner.Writer.Impl());
    }

    default ActionPrinter stdout() {
      return this.apply(ReportingActionRunner.Writer.Std.OUT);
    }

    default ActionPrinter stderr() {
      return this.apply(ReportingActionRunner.Writer.Std.ERR);
    }

    default ActionPrinter trace() {
      return this.apply(ReportingActionRunner.Writer.Slf4J.TRACE);
    }

    default ActionPrinter debug() {
      return this.apply(ReportingActionRunner.Writer.Slf4J.DEBUG);
    }

    default ActionPrinter info() {
      return this.apply(ReportingActionRunner.Writer.Slf4J.INFO);
    }

    default ActionPrinter warn() {
      return this.apply(ReportingActionRunner.Writer.Slf4J.WARN);
    }

    default ActionPrinter error() {
      return this.apply(ReportingActionRunner.Writer.Slf4J.ERROR);
    }
  }

  /**
   * A simple visitor that prints actions.
   * Typically, an instance of this class will be applied to a given action in a following manner.
   * <p/>
   * <code>
   * action.accept(new Impl());
   * </code>
   */
  public static class Impl extends ActionPrinter {

    /**
     * Creates an object of this class.
     *
     * @param writer A writer through which this object's output is printed.
     * @see ReportingActionRunner.Writer
     */
    public Impl(ReportingActionRunner.Writer writer) {
      super(writer);
      this.indent = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Action action) {
      writeLine(this.describeAction(action));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Named action) {
      writeLine(describeAction(action));
      enter(action);
      try {
        action.getAction().accept(this);
      } finally {
        leave(action);
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Composite action) {
      writeLine(describeAction(action));
      enter(action);
      try {
        for (Action child : action) {
          child.accept(this);
        }
      } finally {
        leave(action);
      }
    }

    @Override
    public <T> void visit(ForEach<T> action) {
      action.createHandler(() -> {
        throw notPrintable();
      }).accept(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E extends Throwable> void visit(Attempt<E> action) {
      writeLine(describeAction(action));
      enter(action);
      try {
        action.attempt().accept(this);
        action.recover(() -> {
          throw notPrintable();
        }).accept(this);
        action.ensure().accept(this);
      } finally {
        leave(action);
      }
    }

    @Override
    public void visit(TestAction action) {
      writeLine(describeAction(action));
      enter(action);
      try {
        action.given().accept(this);
        action.when().accept(this);
        action.then().accept(this);
      } finally {
        leave(action);
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Retry action) {
      writeLine(describeAction(action));
      enter(action);
      try {
        action.action.accept(this);
      } finally {
        leave(action);
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(TimeOut action) {
      writeLine(describeAction(action));
      enter(action);
      try {
        action.action.accept(this);
      } finally {
        leave(action);
      }
    }

    /**
     * Returns a writer of this object.
     */
    public ReportingActionRunner.Writer getWriter() {
      return this.writer;
    }
  }
}
