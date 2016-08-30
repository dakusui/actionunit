package com.github.dakusui.actionunit;

import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.AbstractList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class TestUtils {
  public static boolean isRunUnderSurefire() {
    return System.getProperty("surefire.real.class.path") != null;
  }

  public static class Out extends AbstractList<String> {
    private List<String> out = new LinkedList<>();

    public void println(String s) {
      if (!isRunUnderSurefire()) {
        System.out.println(s);
      }
      this.out.add(s);
    }

    @Override
    public String get(int index) {
      return out.get(index);
    }

    @Override
    public Iterator<String> iterator() {
      return out.iterator();
    }

    @Override
    public int size() {
      return out.size();
    }
  }

  /**
   * A base class for tests which writes to stdout/stderr.
   */
  public static class StdOutTestBase {
    PrintStream stdout = System.out;
    PrintStream stderr = System.err;

    @Before
    public void suppressStdOutErr() {
      if (TestUtils.isRunUnderSurefire()) {
        System.setOut(new PrintStream(new OutputStream() {
          @Override
          public void write(int b) throws IOException {
          }
        }));
        System.setErr(new PrintStream(new OutputStream() {
          @Override
          public void write(int b) throws IOException {
          }
        }));
      }
    }

    @After
    public void restoreStdOutErr() {
      System.setOut(stdout);
      System.setOut(stderr);
    }
  }
}
