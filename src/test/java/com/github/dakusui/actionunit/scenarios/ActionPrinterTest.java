package com.github.dakusui.actionunit.scenarios;

import com.github.dakusui.actionunit.compat.actions.ActionBase;
import com.github.dakusui.actionunit.compat.actions.Composite;
import com.github.dakusui.actionunit.compat.core.Context;
import com.github.dakusui.actionunit.compat.utils.Matchers;
import com.github.dakusui.actionunit.compat.utils.TestUtils;
import com.github.dakusui.actionunit.compat.visitors.PrintingActionScanner;
import com.github.dakusui.actionunit.compat.visitors.reporting.ReportingActionPerformer;
import com.github.dakusui.actionunit.n.core.Action;
import com.github.dakusui.actionunit.n.exceptions.ActionException;
import com.github.dakusui.actionunit.n.io.Writer;
import com.github.dakusui.actionunit.n.visitors.ActionPrinter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

import static com.github.dakusui.actionunit.compat.utils.TestUtils.hasItemAt;
import static com.github.dakusui.actionunit.compat.utils.TestUtils.size;
import static com.github.dakusui.actionunit.n.core.ActionSupport.*;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;

@RunWith(Enclosed.class)
public class ActionPrinterTest extends TestUtils.TestBase {
  private static class ActionComposer extends TestUtils.TestBase {
    Action composeAction() {
      return named("Concurrent (top level)",
          parallel(
              named("Sequential (1st child)",
                  sequential(
                      simple("simple1", (context) -> {
                      }),
                      simple("simple2", (context) -> {
                      }),
                      simple("simple3", (context) -> {
                      }),
                      forEach(
                          "i",
                          () -> asList("hello1", "hello2", "hello3").stream()
                      ).perform(
                          nop()
                      )
                  ))));
    }
  }

  public static class ImplTest extends ActionComposer {

    @Test
    public void givenTrace() {
      composeAction().accept(new ActionPrinter(Writer.Slf4J.TRACE));
    }

    @Test
    public void givenDebug$whenTestActionAccepts$thenNoErrorWillBeGiven() {
      composeAction().accept(new ActionPrinter(Writer.Slf4J.DEBUG));
    }

    @Test
    public void givenInfo$whenTestActionAccepts$thenNoErrorWillBeGiven() {
      composeAction().accept(new ActionPrinter(Writer.Slf4J.INFO));
    }

    @Test
    public void givenWarn() {
      composeAction().accept(new ActionPrinter(Writer.Slf4J.WARN));
    }

    @Test
    public void givenError() {
      composeAction().accept(new ActionPrinter(Writer.Slf4J.ERROR));
    }

    @Test
    public void givenNew() {
      ////
      // Prepare and run a printing scanner
      Writer.Impl writer = new Writer.Impl();
      ActionPrinter printer = new ActionPrinter(writer);
      // run printing scanner
      composeAction().accept(printer);
      // print data written to a writer
      StreamSupport.stream(writer.spliterator(), false).forEach(System.err::println);

      Iterator<String> i = writer.iterator();
      assertThat(i.next(), containsString("Concurrent (top level)"));
      assertThat(i.next(), containsString("Concurrent (1 actions)"));
      assertThat(i.next(), containsString("Sequential (1st child)"));
      assertThat(i.next(), containsString("Sequential (4 actions)"));
      assertThat(i.next(), containsString("simple1"));
      assertThat(i.next(), containsString("simple2"));
      assertThat(i.next(), containsString("simple3"));
      assertThat(i.next(), containsString("ForEach"));
      assertThat(i.next(), containsString("(nop)"));
      assertEquals(9, size(writer));
    }
  }

  public static class StdOutErrTest extends ActionComposer {
    @Test
    public void givenStdout$whenTestActionAccepts$thenNoErrorWillBeGiven() {
      composeAction().accept(new ActionPrinter(Writer.Std.OUT));
    }

    @Test
    public void givenStderr$whenTestActionAccepts$thenNoErrorWillBeGiven() {
      composeAction().accept(new ActionPrinter(Writer.Std.ERR));
    }
  }

    @Test
    public void givenComplicatedTestAction$whenPerformed$thenWorksFine() {
      List<String> out = new LinkedList<>();
      Action action = composeAction();
      try {
        action.accept(TestUtils.createActionPerformer());
        assertEquals(asList("hello:hello1", "hello:hello2", "hello:hello3"), out);
      } finally {
        runAndReport(action, new TestUtils.Out());
      }
    }

    @Test
    public void givenComplicatedTestAction$whenPrinted$thenPrintedCorrectly() {
      List<String> out = new LinkedList<>();
      PrintingActionScanner printer = new PrintingActionScanner(new Writer.Impl());
      Writer.Impl writer = (Writer.Impl) printer.getWriter();
      composeAction(out).accept(printer);
      StreamSupport.stream(writer.spliterator(), false).forEach(System.err::println);
      Iterator<String> i = writer.iterator();
      assertThat(i.next(), containsString("Concurrent (top level)"));
      assertThat(i.next(), containsString("Concurrent"));
      assertThat(i.next(), containsString("Sequential (1st child)"));
      assertThat(i.next(), containsString("Sequential"));
      assertThat(i.next(), containsString("simple1"));
      assertThat(i.next(), containsString("simple2"));
      assertThat(i.next(), containsString("simple3"));
      assertThat(i.next(), containsString("ForEach1"));
      assertThat(i.next(), containsString("ForEach"));
      assertThat(i.next(), containsString("TestAction"));
      assertThat(i.next(), containsString("Given"));
      i.next();
      assertThat(i.next(), containsString("When"));
      i.next();
      assertThat(i.next(), containsString("Then"));
      i.next();
      assertThat(i.next(), containsString("ForEach2"));
      assertThat(i.next(), containsString("ForEach"));
      assertThat(i.next(), containsString("Sequential"));
      assertThat(i.next(), containsString("nothing"));
      assertThat(i.next(), containsString("sink1"));
      assertThat(i.next(), containsString("sink2"));
      assertEquals(22, size(writer));
    }
  }

  public static class WithResultVariationTest extends TestUtils.ContextTestBase implements Context {
    @Test
    public void givenForEachWithTag$whenPerformed$thenResultPrinted() {
      final TestUtils.Out out1 = new TestUtils.Out();
      Action action = forEachOf(
          asList("A", "B")
      ).perform(
          i -> $ -> $.sequential(
              $.simple("+0", () -> out1.writeLine(i.get() + "0")),
              $.simple("+1", () -> out1.writeLine(i.get() + "1"))
          )
      );
      action.accept(TestUtils.createActionPerformer());
      assertEquals(asList("A0", "A1", "B0", "B1"), out1);

      final TestUtils.Out out2 = new TestUtils.Out();
      runAndReport(action, out2);
      assertThat(
          out2,
          allOf(
              hasItemAt(0, Matchers.allOf(containsString("[.]"), containsString("ForEach"))),
              hasItemAt(1, Matchers.allOf(containsString("[..]"), containsString("Sequential"))),
              hasItemAt(2, Matchers.allOf(containsString("[..]"), containsString("+0"))),
              hasItemAt(3, Matchers.allOf(containsString("[..]"), containsString("+1")))
          ));
      Assert.assertThat(
          out2.size(),
          equalTo(4)
      );
    }

    @Test
    public void givenRetryAction$whenPerformed$thenResultPrinted() {
      Action action = retry(nop()).times(1).withIntervalOf(1, TimeUnit.MINUTES).build();
      TestUtils.Out out = new TestUtils.Out();
      runAndReport(action, out);
      assertThat(
          out.get(0),
          allOf(
              containsString("[.]"),
              containsString("Retry(60[seconds]x1times)")
          ));
    }

    @Test(expected = IllegalStateException.class)
    public void givenFailingRetryAction$whenPerformed$thenResultPrinted() {
      final TestUtils.Out out = new TestUtils.Out();
      Action action = retry(simple("AlwaysFail", new Runnable() {
        @Override
        public void run() {
          throw new IllegalStateException(this.toString());
        }
      })).times(1).withIntervalOf(1, TimeUnit.MINUTES).build();
      try {
        runAndReport(action, out);
      } finally {
        assertThat(
            out.get(0),
            allOf(
                containsString("[E]"),
                containsString("Retry(60[seconds]x1times)")
            ));
        assertThat(
            out.get(1),
            allOf(
                containsString("[E]"),
                containsString("AlwaysFail")
            ));
      }
    }

    @Test
    public void givenPassAfterRetryAction$whenPerformed$thenResultPrinted() {
      final TestUtils.Out outForRun = new TestUtils.Out();
      Action action = retry(
          simple("PassAfterFail", new Runnable() {
            boolean tried = false;

            @Override
            public void run() {
              try {
                if (!tried) {
                  outForRun.writeLine("PassAfterFail");
                  throw new ActionException(this.toString());
                }
              } finally {
                tried = true;
              }
            }
          })).times(1).withIntervalOf(1, MILLISECONDS).build();
      runAndReport(action, outForRun);
      assertThat(
          outForRun,
          Matchers.allOf(
              TestUtils.<TestUtils.Out, String>matcherBuilder().transform(
                  "get(0)", (TestUtils.Out v) -> v.get(0)
              ).check(
                  "contains('PassAfterFail')", (String u) -> u.equals("PassAfterFail")
              ),
              TestUtils.<TestUtils.Out, String>matcherBuilder().transform(
                  "get(1)", (TestUtils.Out v) -> v.get(1)
              ).check(
                  "contains('[.]')", (String u) -> u.contains("[.]")
              ),
              TestUtils.<TestUtils.Out, String>matcherBuilder().transform(
                  "get(1)", (TestUtils.Out v) -> v.get(1)
              ).check(
                  "contains('Retry(1[milliseconds]x1times')", (String u) -> u.contains("Retry(1[milliseconds]x1times")
              ),
              TestUtils.<TestUtils.Out, String>matcherBuilder().transform(
                  "get(2)", (TestUtils.Out v) -> v.get(2)
              ).check(
                  "contains('[xo]')", (String u) -> u.contains("[E.]")
              ),
              TestUtils.<TestUtils.Out, String>matcherBuilder().transform(
                  "get(2)", (TestUtils.Out v) -> v.get(2)
              ).check(
                  "contains('PassAfterFail')", (String u) -> u.contains("PassAfterFail")
              )
          )
      );
    }

    @Test
    public void givenTimeoutAction$whenPerformed$thenResultPrinted() {
      Action action = timeout(nop()).in(1, TimeUnit.MINUTES);
      final TestUtils.Out out = new TestUtils.Out();
      runAndReport(action, out);
      assertThat(
          out.get(0),
          allOf(
              containsString("[.]"),
              containsString("TimeOut(60[seconds])")
          ));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void givenUnsupportedCompositeAction$whenPerformed$thenExceptionThrown() {
      Action action = new Composite.Base(0, "noname", Collections.emptyList()) {
        @Override
        public void accept(Visitor visitor) {
          visitor.visit(this);
        }
      };
      TestUtils.createReportingActionPerformer(action).performAndReport();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void givenUnsupportedSimpleAction$whenPerformed$thenExceptionThrown() {
      Action action = new ActionBase(0) {
        @Override
        public void accept(Visitor visitor) {
          visitor.visit(this);
        }
      };
      TestUtils.createReportingActionPerformer(action).performAndReport();
    }
  }

  private static void runAndReport(Action action, TestUtils.Out out) {
    new ReportingActionPerformer.Builder(action).to(out).build().performAndReport();
  }
}
