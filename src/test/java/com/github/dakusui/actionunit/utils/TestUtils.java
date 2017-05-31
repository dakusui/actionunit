package com.github.dakusui.actionunit.utils;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.sandbox.AutocloseableIterator;
import com.github.dakusui.actionunit.sandbox.Autocloseables;
import com.github.dakusui.actionunit.helpers.Utils;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.visitors.ActionPerformer;
import com.github.dakusui.actionunit.visitors.PrintingActionScanner;
import com.github.dakusui.actionunit.visitors.reporting.ReportingActionPerformer;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.dakusui.actionunit.helpers.Checks.checkNotNull;

public class TestUtils {
  static boolean isRunUnderSurefire() {
    return System.getProperty("surefire.real.class.path") != null;
  }

  public static <T> Matcher<Iterable<? super T>> hasItemAt(int position, Matcher<? super T> itemMatcher) {
    return new HasItemAt<>(position, itemMatcher);
  }

  public static <P extends Action.Visitor> P createPrintingActionScanner() {
    return createPrintingActionScanner(Writer.Std.OUT);
  }

  @SuppressWarnings("unchecked")
  public static <P extends Action.Visitor> P createPrintingActionScanner(Writer writer) {
    return (P) PrintingActionScanner.Factory.DEFAULT_INSTANCE.create(writer);
  }

  public static ActionPerformer createActionPerformer() {
    return new ActionPerformer();
  }

  public static ReportingActionPerformer createReportingActionPerformer(Action action) {
    return new ReportingActionPerformer.Builder(action).to(Writer.Std.OUT).build();
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
      Iterator<O> o = Utils.transform(i, func);
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
            Utils.transform(i, func),
            Autocloseables.toAutocloseable(i));
      }

      @Override
      public int size() {
        return in.size();
      }
    };
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

  /**
   * A base class for tests which writes to stdout/stderr.
   */
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

  public static class MatcherBuilder<V, U> {
    String         predicateName = "P";
    Predicate<U>   p             = null;
    String         functionName  = "f";
    Function<V, U> f             = null;

    public MatcherBuilder<V, U> f(String name, Function<V, U> f) {
      this.functionName = Objects.requireNonNull(name);
      this.f = Objects.requireNonNull(f);
      return this;
    }

    public MatcherBuilder<V, U> p(String name, Predicate<U> p) {
      this.predicateName = Objects.requireNonNull(name);
      this.p = Objects.requireNonNull(p);
      return this;
    }

    public Matcher<V> build() {
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
          .f("passthrough", t -> t);
    }

    public static <T, U> MatcherBuilder<T, U> create() {
      return new MatcherBuilder<>();
    }
  }
}