package com.github.dakusui.actionunit.visitors;


import com.github.dakusui.actionunit.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.github.dakusui.actionunit.Utils.describe;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A simple visitor that prints actions.
 * Typically, an instance of this class will be applied to a given action in a following manner.
 * <p/>
 * <code>
 * action.accept(new ActionPrinter());
 * </code>
 */
public class ActionPrinter<W extends ActionPrinter.Writer> extends Action.Visitor.Base {
  /*
   * A writer through which this object's output is printed.
   */
  private final W writer;

  /*
   * current indent level.
   */
  private int indent;

  /**
   * Creates an object of this class.
   *
   * @param writer A writer through which this object's output is printed.
   * @see Writer
   */
  public ActionPrinter(W writer) {
    this.writer = checkNotNull(writer);
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
  public void visit(Action.Composite action) {
    writeLine(describeAction(action));
    indent++;
    try {
      for (Action child : action) {
        child.accept(this);
      }
    } finally {
      indent--;
    }
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(Action.With action) {
    writeLine(describeAction(action));
    if (!(action instanceof Action.Piped)) {
      indent++;
      try {
        action.getAction().accept(this);
      } finally {
        indent--;
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void visit(Action.ForEach action) {
    writeLine(describeAction(action));
    indent++;
    try {
      action.getAction().accept(this);
    } finally {
      indent--;
    }
  }

  /**
   * Returns a writer of this object.
   */
  public W getWriter() {
    return this.writer;
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
    this.writer.writeLine(indent(this.indent) + s);
  }

  /**
   * An extension point to customize indentation style.
   *
   * @param indent Level of indentation.
   */
  protected String indent(int indent) {
    String ret = "";
    for (int i = 0; i < indent; i++) {
      ret += indent();
    }
    return ret;
  }

  /**
   * An extension point to customize a string used for indentation.
   */
  protected String indent() {
    return "  ";
  }

  /**
   * An interface that abstracts various destinations to which {@link ActionPrinter}'s
   * output goes.
   */
  public interface Writer {
    void writeLine(String s);

    class Impl implements Writer, Iterable<String> {
      List<String> arr = new ArrayList<>();

      @Override
      public void writeLine(String s) {
        arr.add(s);
      }

      @Override
      public Iterator<String> iterator() {
        return this.arr.iterator();
      }
    }

    enum Std implements Writer {
      OUT(System.out),
      ERR(System.err);

      private final PrintStream printStream;

      Std(PrintStream printStream) {
        this.printStream = printStream;
      }

      @Override
      public void writeLine(String s) {
        this.printStream.println(s);
      }
    }

    enum Slf4J implements Writer {
      TRACE {
        @Override
        public void writeLine(String s) {
          LOGGER.trace(s);
        }
      },
      DEBUG {
        @Override
        public void writeLine(String s) {
          LOGGER.debug(s);
        }
      },
      INFO {
        @Override
        public void writeLine(String s) {
          LOGGER.info(s);
        }
      },
      WARN {
        @Override
        public void writeLine(String s) {
          LOGGER.warn(s);
        }
      },
      ERROR {
        @Override
        public void writeLine(String s) {
          LOGGER.error(s);
        }
      };
      private static final Logger LOGGER = LoggerFactory.getLogger(Slf4J.class);
    }
  }

  /**
   * A factory class to create {@link ActionPrinter} objects.
   */
  public enum Factory {
    ;

    public static ActionPrinter<Writer.Impl> create() {
      return new ActionPrinter<>(new Writer.Impl());
    }

    public static ActionPrinter stdout() {
      return new ActionPrinter<>(Writer.Std.OUT);
    }

    public static ActionPrinter stderr() {
      return new ActionPrinter<>(Writer.Std.ERR);
    }

    public static ActionPrinter trace() {
      return new ActionPrinter<>(Writer.Slf4J.TRACE);
    }

    public static ActionPrinter debug() {
      return new ActionPrinter<>(Writer.Slf4J.DEBUG);
    }

    public static ActionPrinter info() {
      return new ActionPrinter<>(Writer.Slf4J.INFO);
    }

    public static ActionPrinter warn() {
      return new ActionPrinter<>(Writer.Slf4J.WARN);
    }

    public static ActionPrinter error() {
      return new ActionPrinter<>(Writer.Slf4J.ERROR);
    }
  }
}