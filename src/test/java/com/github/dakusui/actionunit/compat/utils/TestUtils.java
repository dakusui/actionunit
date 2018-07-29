package com.github.dakusui.actionunit.compat.utils;

import com.github.dakusui.actionunit.n.core.Action;
import com.github.dakusui.actionunit.n.exceptions.ActionException;
import com.github.dakusui.actionunit.n.io.Writer;
import com.github.dakusui.actionunit.n.visitors.ActionPerformer;
import com.github.dakusui.actionunit.n.visitors.ActionPrinter;
import com.github.dakusui.actionunit.n.visitors.ReportingActionPerformer;
import com.github.dakusui.actionunit.n.visitors.SimpleActionPerformer;
import com.github.dakusui.actionunit.sandbox.AutocloseableIterator;
import com.github.dakusui.actionunit.sandbox.Autocloseables;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.dakusui.actionunit.n.core.ActionSupport.simple;
import static com.github.dakusui.actionunit.n.utils.Checks.checkArgument;
import static com.github.dakusui.actionunit.n.utils.Checks.checkNotNull;

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

  public static <T> Matcher<Iterable<? super T>> hasItemAt(int position, Matcher<? super T> itemMatcher) {
    return new HasItemAt<>(position, itemMatcher);
  }

  public static <P extends Action.Visitor> P createPrintingActionScanner() {
    return createPrintingActionScanner(Writer.Std.OUT);
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

  public static <I, O> AutocloseableIterator<O> transform(final AutocloseableIterator<I> in, final Function<? super I, ? extends O> function) {
    return new AutocloseableIterator<O>() {
      @Override
      public void close() {
        in.close();
      }

      @Override
      public boolean hasNext() {
        return in.hasNext();
      }

      @Override
      public O next() {
        return function.apply(in.next());
      }

      @Override
      public void remove() {
        in.remove();
      }
    };
  }

  public static <I, O> Iterable<O> transform(final Iterable<I> in, final Function<? super I, ? extends O> func) {
    if (in instanceof Collection) {
      //noinspection unchecked,RedundantCast
      return (Collection<O>) transformCollection((Collection<I>) in, (Function<? super I, O>) func);
    }
    return transformIterable(in, func);
  }

  public static <I, O> Iterable<O> transformIterable(final Iterable<I> in, final Function<? super I, ? extends O> func) {
    checkNotNull(func);
    return (AutocloseableIterator.Factory<O>) () -> {
      Iterator<I> i = in.iterator();
      Iterator<O> o = transform(i, func);
      return Autocloseables.autocloseable(
          o,
          Autocloseables.toAutocloseable(i)
      );
    };
  }

  public static <I, O> Collection<O> transformCollection(final Collection<I> in, final Function<? super I, O> func) {
    checkNotNull(func);
    return new AbstractCollection<O>() {
      @Override
      public void clear() {
        in.clear();
      }

      @Override
      public boolean isEmpty() {
        return in.isEmpty();
      }

      @Override
      public Iterator<O> iterator() {
        Iterator<I> i = in.iterator();
        return Autocloseables.autocloseable(
            transform(i, func),
            Autocloseables.toAutocloseable(i));
      }

      @Override
      public int size() {
        return in.size();
      }
    };
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

  public static <T, U> Iterator<U> transform(Iterator<T> iterator, Function<? super T, ? extends U> func) {
    checkNotNull(iterator);
    checkNotNull(func);
    return new Iterator<U>() {

      @Override
      public boolean hasNext() {
        return iterator.hasNext();
      }

      @Override
      public U next() {
        return func.apply(iterator.next());
      }
    };
  }

  public static <T> int size(Iterable<? super T> iterable) {
    if (iterable instanceof Collection)
      return ((Collection) iterable).size();
    return new LinkedList<T>() {{
      for (Object i : iterable) {
        add((T) i);
      }
    }}.size();
  }

  public static <T, U> MatcherBuilder<T, U> matcherBuilder() {
    return new MatcherBuilder<>();
  }

  public static Out performAndReportAction(Action action) {
    final Out result = new Out();
    ReportingActionPerformer.create(Writer.Std.OUT).performAndReport(action);
    return result;
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

  /**
   * A base class for tests which writes to stdout/stderr.
   */
  public static class ContextTestBase extends TestBase {
  }

  public static class MatcherBuilder<V, U> {
    String       predicateName = null;
    Predicate<U> p             = null;
    String       functionName  = "";
    @SuppressWarnings("unchecked")
    Function<V, U> f = v -> (U) v;

    public MatcherBuilder<V, U> transform(String name, Function<V, U> f) {
      this.functionName = Objects.requireNonNull(name);
      this.f = Objects.requireNonNull(f);
      return this;
    }

    public Matcher<V> check(String name, Predicate<U> p) {
      this.predicateName = Objects.requireNonNull(name);
      this.p = Objects.requireNonNull(p);
      return this.build();
    }

    private Matcher<V> build() {
      Objects.requireNonNull(p);
      Objects.requireNonNull(f);
      return new BaseMatcher<V>() {
        @SuppressWarnings("unchecked")
        @Override
        public boolean matches(Object item) {
          return p.test(f.apply((V) item));
        }

        @SuppressWarnings("unchecked")
        @Override
        public void describeMismatch(Object item, Description description) {
          description
              .appendText("was false because " + functionName + "(x)=")
              .appendValue(f.apply((V) item))
              .appendText("; x=")
              .appendValue(item)
          ;
        }

        @Override
        public void describeTo(Description description) {
          description.appendText(String.format("%s(%s(x))", predicateName, functionName));
        }
      };
    }

    public static <T> MatcherBuilder<T, T> simple() {
      return new MatcherBuilder<T, T>()
          .transform("passthrough", t -> t);
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
