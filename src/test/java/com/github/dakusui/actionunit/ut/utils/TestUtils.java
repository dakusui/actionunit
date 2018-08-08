package com.github.dakusui.actionunit.ut.utils;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.visitors.ActionPerformer;
import com.github.dakusui.actionunit.visitors.ActionPrinter;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import com.github.dakusui.actionunit.visitors.SimpleActionPerformer;
import org.junit.After;
import org.junit.Before;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.github.dakusui.actionunit.core.ActionSupport.simple;
import static com.github.dakusui.actionunit.utils.Checks.checkArgument;

public class TestUtils {
  public static <T> List<T> toList(Iterable<T> iterable) {
    return new LinkedList<T>() {{
      for (T i : iterable) {
        add(i);
      }
    }};
  }

  /**
   * types of Operating Systems
   */
  public enum OSType {
    Windows, MacOS, Linux, Other
  }

  /**
   * detect the operating system from the os.name System property and cache
   * the result
   *
   * @return - the operating system detected
   */
  private static OSType getOperatingSystemType(Properties properties) {
    OSType detectedOS;
    String OS = properties.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
    if ((OS.contains("mac")) || (OS.contains("darwin"))) {
      detectedOS = OSType.MacOS;
    } else if (OS.contains("win")) {
      detectedOS = OSType.Windows;
    } else if (OS.contains("nux")) {
      detectedOS = OSType.Linux;
    } else {
      detectedOS = OSType.Other;
    }

    return detectedOS;
  }

  private static boolean isRunUnderSurefire() {
    return System.getProperty("surefire.real.class.path") != null;
  }

  public static boolean isRunUnderLinux() {
    return getOperatingSystemType(System.getProperties()).equals(OSType.Linux);
  }

  public static boolean isRunByTravis() {
    return Objects.equals(System.getProperty("user.name"), "travis");
  }

  @SuppressWarnings("unchecked")
  public static <P extends Action.Visitor> P createPrintingActionScanner(Writer writer) {
    return (P) new ActionPrinter(writer);
  }

  public static ActionPerformer createActionPerformer() {
    return SimpleActionPerformer.create();
  }

  public static ReportingActionPerformer createReportingActionPerformer() {
    return ReportingActionPerformer.create(Writer.Std.OUT);
  }

  public static Action sleep(long duration, TimeUnit timeUnit) {
    return simple(String.format("sleep %s[%s]", duration, timeUnit), context -> {
      try {
        Thread.sleep(timeUnit.toMillis(duration));
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw ActionException.wrap(e);
      }
    });
  }

  /**
   * Equivalent to {@code range(0, stop)}.
   *
   * @see TestUtils#range(int, int)
   */
  public static Iterable<Integer> range(int stop) {
    return range(0, stop);
  }

  /**
   * Returns an iterable object that covers specified range by arguments given
   * to this method.
   *
   * @param start A number from which returned iterable object starts
   * @param stop  A number at which returned iterable object stops
   * @param step  A number by which returned iterable object increases. positive
   *              and negative are allowed, but zero is not.
   */
  public static Iterable<Integer> range(final int start, final int stop, final int step) {
    checkArgument(step != 0, "step argument must not be zero. ");
    return () -> new Iterator<Integer>() {
      long current = start;

      @Override
      public boolean hasNext() {
        long next = current + step;
        // If next value goes over int range, returned iterator will stop.
        //noinspection SimplifiableIfStatement
        if (next > Integer.MAX_VALUE || next < Integer.MIN_VALUE)
          return false;
        return Math.signum(step) > 0
            ? next <= stop
            : next >= stop;
      }

      @Override
      public Integer next() {
        try {
          return (int) current;
        } finally {
          current += step;
        }
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  /**
   * Equivalent to {@code range(start, stop, 1)}.
   *
   * @see TestUtils#range(int, int, int)
   */
  public static Iterable<Integer> range(int start, int stop) {
    return range(start, stop, 1);
  }

  @SuppressWarnings("unchecked")
  public static <T> int size(Iterable<? super T> iterable) {
    if (iterable instanceof Collection)
      return ((Collection) iterable).size();
    return new LinkedList<T>() {{
      for (Object i : iterable) {
        add((T) i);
      }
    }}.size();
  }

  public static class Out extends AbstractList<String> implements Writer {
    private List<String> out = new LinkedList<>();

    public void writeLine(String s) {
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

  public static class TestBase {
    PrintStream stdout = System.out;
    PrintStream stderr = System.err;
    final protected List<String> out = Collections.synchronizedList(new LinkedList<>());

    @Before
    public void suppressStdOutErr() {
      if (TestUtils.isRunUnderSurefire()) {
        System.setOut(new PrintStream(new OutputStream() {
          @Override
          public void write(int b) {
          }
        }));
        System.setErr(new PrintStream(new OutputStream() {
          @Override
          public void write(int b) {
          }
        }));
      }
    }

    @After
    public void restoreStdOutErr() {
      System.setOut(stdout);
      System.setOut(stderr);
    }

    protected void printf(String format, Object... args) {
      String s = String.format(format, args);
      System.out.println(s);
      out.add(s);
    }
  }

  public static <I, O> Function<I, O> memoize(Function<I, O> function) {
    return new Function<I, O>() {
      Map<I, O> cache = new ConcurrentHashMap<>();

      @Override
      public O apply(I i) {
        return cache.computeIfAbsent(i, function);
      }
    };
  }
}
