package com.github.dakusui.actionunit.visitors;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.helpers.Utils;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.visitors.reporting.Node;

import java.util.Objects;
import java.util.function.Function;

import static com.github.dakusui.actionunit.helpers.InternalUtils.describe;

public class PrintingActionScanner extends ActionScanner {
  private final Writer writer;

  public PrintingActionScanner(Writer writer) {
    this.writer = Objects.requireNonNull(writer);
  }

  @Override
  protected <A extends Action> void before(Node<A> node) {
    writeLine(describeAction(node.getContent()));
    super.before(node);
  }

  /**
   * An extension point to customize format a line to be printed by this object.
   *
   * @param s A line to be printed. Typically a description of an action.
   */
  protected void writeLine(String s) {
    boolean first = true;
    for (String each : s.split("\\n")) {
      this.writer.writeLine(Utils.spaces((this.getCurrentPath().size() + (first ? 0 : 1)) * 2) + each);
      first = false;
    }
  }

  /**
   * An extension point to customize format to print out each {@link Action} object.
   *
   * @param action an {@code Action} to be printed.
   * @return description of the given action.
   */
  @SuppressWarnings("WeakerAccess")
  protected String describeAction(Action action) {
    return describe(action);
  }

  public Writer getWriter() {
    return writer;
  }

  public interface Factory extends Function<Writer, PrintingActionScanner> {
    Factory DEFAULT_INSTANCE = PrintingActionScanner::new;

    default PrintingActionScanner create(Writer writer) {
      return this.apply(writer);
    }

    default PrintingActionScanner create() {
      return this.apply(new Writer.Impl());
    }

    default PrintingActionScanner stdout() {
      return this.apply(Writer.Std.OUT);
    }

    default PrintingActionScanner stderr() {
      return this.apply(Writer.Std.ERR);
    }

    default PrintingActionScanner trace() {
      return this.apply(Writer.Slf4J.TRACE);
    }

    default PrintingActionScanner debug() {
      return this.apply(Writer.Slf4J.DEBUG);
    }

    default PrintingActionScanner info() {
      return this.apply(Writer.Slf4J.INFO);
    }

    default PrintingActionScanner warn() {
      return this.apply(Writer.Slf4J.WARN);
    }

    default PrintingActionScanner error() {
      return this.apply(Writer.Slf4J.ERROR);
    }
  }
}
