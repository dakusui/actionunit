package com.github.dakusui.actionunit.visitors;


import com.github.dakusui.actionunit.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings("ALL")
public class ActionPrinter extends Action.Visitor.Base implements Iterable<String> {
  private final Writer writer;

  private int indent;

  public ActionPrinter(Writer writer) {
    this.writer = checkNotNull(writer);
    this.indent = 0;
  }

  @Override
  public void visit(Action action) {
    writer.writeLine(indent(this.indent) + action.describe());
  }

  @Override
  public void visit(Action.Composite action) {
    writer.writeLine(indent(this.indent) + action.describe());
    indent++;
    try {
      for (Action child : action) {
        child.accept(this);
      }
    } finally {
      indent--;
    }
  }

  @Override
  public void visit(Action.With action) {

  }

  @Override
  public Iterator<String> iterator() {
    return this.writer.iterator();
  }

  public static ActionPrinter create() {
    return new ActionPrinter(new Writer.Impl());
  }

  public static ActionPrinter stdout() {
    return new ActionPrinter(Writer.Std.OUT);
  }

  public static ActionPrinter stderr() {
    return new ActionPrinter(Writer.Std.ERR);
  }

  public static ActionPrinter trace() {
    return new ActionPrinter(Writer.Slf4J.TRACE);
  }

  public static ActionPrinter debug() {
    return new ActionPrinter(Writer.Slf4J.DEBUG);
  }

  public static ActionPrinter info() {
    return new ActionPrinter(Writer.Slf4J.INFO);
  }

  public static ActionPrinter warn() {
    return new ActionPrinter(Writer.Slf4J.WARN);
  }

  public static ActionPrinter error() {
    return new ActionPrinter(Writer.Slf4J.ERROR);
  }

  private static String indent(int indent) {
    String ret = "";
    for (int i = 0; i < indent; i++) {
      ret += "  ";
    }
    return ret;
  }

  public interface Writer extends Iterable<String> {
    void writeLine(String s);

    class Impl implements Writer {
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

      @Override
      public Iterator<String> iterator() {
        throw new UnsupportedOperationException();
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

      @Override
      public Iterator<String> iterator() {
        throw new UnsupportedOperationException();
      }
    }
  }
}
