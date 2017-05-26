package com.github.dakusui.actionunit.visitors;

import com.github.dakusui.actionunit.actions.*;
import com.github.dakusui.actionunit.compat.visitors.CompatActionPrinter;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.helpers.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import static com.github.dakusui.actionunit.helpers.Checks.checkNotNull;
import static com.github.dakusui.actionunit.helpers.Checks.notPrintable;
import static com.github.dakusui.actionunit.helpers.Utils.describe;

public abstract class ActionPrinter extends Action.Visitor.Base {
  /*
   * A writer through which this object's output is printed.
   */
  protected final Writer writer;
  /*
   * current indent level.
   */
  @SuppressWarnings("WeakerAccess")
  protected       int    indent;

  public ActionPrinter(Writer writer) {
    this.writer = checkNotNull(writer);
  }

  public interface Factory extends Function<Writer, ActionPrinter> {
    Factory DEFAULT_INSTANCE = Impl::new;

    default ActionPrinter create(Writer writer) {
      return this.apply(writer);
    }

    default ActionPrinter create() {
      return this.apply(new Writer.Impl());
    }

    default ActionPrinter stdout() {
      return this.apply(Writer.Std.OUT);
    }

    default ActionPrinter stderr() {
      return this.apply(Writer.Std.ERR);
    }

    default ActionPrinter trace() {
      return this.apply(Writer.Slf4J.TRACE);
    }

    default ActionPrinter debug() {
      return this.apply(Writer.Slf4J.DEBUG);
    }

    default ActionPrinter info() {
      return this.apply(Writer.Slf4J.INFO);
    }

    default ActionPrinter warn() {
      return this.apply(Writer.Slf4J.WARN);
    }

    default ActionPrinter error() {
      return this.apply(Writer.Slf4J.ERROR);
    }
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
      this.writer.writeLine(Utils.spaces(this.indent * 2 + (first ? 0 : 1)) + each);
      first = false;
    }
  }

  /**
   * An interface that abstracts various destinations to which {@link ActionPrinter.Impl}'s
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
      OUT {
        @Override
        public void writeLine(String s) {
          System.out.println(s);
        }
      },
      ERR {
        @Override
        public void writeLine(String s) {
          System.err.println(s);
        }
      };

      @Override
      public abstract void writeLine(String s);
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
      private static final Logger LOGGER = LoggerFactory.getLogger(ActionPrinter.Impl.Writer.Slf4J.class);
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
  public static class Impl extends CompatActionPrinter {

    /**
     * Creates an object of this class.
     *
     * @param writer A writer through which this object's output is printed.
     * @see Writer
     */
    public Impl(Writer writer) {
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


    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(When action) {
      writeLine(describeAction(action));
      enter(action);
      try {
        action.getAction().accept(this);
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
    public void visit(While action) {
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
    public Writer getWriter() {
      return this.writer;
    }
  }
}
