package com.github.dakusui.actionunit.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An interface that abstracts various destinations to which {@link com.github.dakusui.actionunit.visitors.PrintingActionScanner}'s
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
    private static final Logger LOGGER = LoggerFactory.getLogger(Slf4J.class);
  }
}
