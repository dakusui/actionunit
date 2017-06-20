package com.github.dakusui.actionunit.scenarios;

import com.github.dakusui.actionunit.actions.ActionBase;
import com.github.dakusui.actionunit.actions.Composite;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.utils.Matchers;
import com.github.dakusui.actionunit.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.PrintingActionScanner;
import com.github.dakusui.actionunit.visitors.reporting.ReportingActionPerformer;
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

import static com.github.dakusui.actionunit.utils.TestUtils.hasItemAt;
import static com.github.dakusui.actionunit.utils.TestUtils.size;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;

@RunWith(Enclosed.class)
public class ActionPrinterTest implements Context {
  private static class ActionComposer extends TestUtils.TestBase implements Context {
    Action composeAction() {
      return named("Concurrent (top level)",
          concurrent(
              named("Sequential (1st child)",
                  sequential(
                      simple("simple1", () -> {
                      }),
                      simple("simple2", () -> {
                      }),
                      simple("simple3", () -> {
                      }),
                      forEachOf(
                          asList("hello1", "hello2", "hello3")
                      ).perform(
                          ($, i) -> nop()
                      )
                  ))));
    }
  }

  public static class ImplTest extends ActionComposer {

    @Test
    public void givenTrace() {
      composeAction().accept(PrintingActionScanner.Factory.DEFAULT_INSTANCE.trace());
    }

    @Test
    public void givenDebug$whenTestActionAccepts$thenNoErrorWillBeGiven() {
      composeAction().accept(PrintingActionScanner.Factory.DEFAULT_INSTANCE.debug());
    }

    @Test
    public void givenInfo$whenTestActionAccepts$thenNoErrorWillBeGiven() {
      composeAction().accept(PrintingActionScanner.Factory.DEFAULT_INSTANCE.info());
    }

    @Test
    public void givenWarn() {
      composeAction().accept(PrintingActionScanner.Factory.DEFAULT_INSTANCE.warn());
    }

    @Test
    public void givenError() {
      composeAction().accept(PrintingActionScanner.Factory.DEFAULT_INSTANCE.error());
    }

    @Test
    public void givenNew() {
      ////
      // Prepare and run a printing scanner
      PrintingActionScanner printer = new PrintingActionScanner(new Writer.Impl());
      Writer.Impl writer = (Writer.Impl) printer.getWriter();
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

  public static class StdOutErrTest extends ActionComposer implements Context {
    @Test
    public void givenStdout$whenTestActionAccepts$thenNoErrorWillBeGiven() {
      composeAction().accept(PrintingActionScanner.Factory.DEFAULT_INSTANCE.stdout());
    }

    @Test
    public void givenStderr$whenTestActionAccepts$thenNoErrorWillBeGiven() {
      composeAction().accept(PrintingActionScanner.Factory.DEFAULT_INSTANCE.stderr());
    }
  }

  public static class WithResultTest extends TestUtils.TestBase implements Context {
    private Action composeAction(final List<String> out) {
      //noinspection unchecked
      return named("Concurrent (top level)", concurrent(
          named("Sequential (1st child)", sequential(
              simple("simple1", () -> {
              }),
              simple("simple2", () -> {
              }),
              simple("simple3", () -> {
              }))
          ),
          named("ForEach1",
              forEachOf(
                  asList("hello1", "hello2", "hello3")
              ).perform(
                  ($, data) -> $.given(
                      "ExampleTest", () -> "ExampleTest")
                      .when("Say 'hello'", input -> {
                        out.add(format("hello:%s", data.get()));
                        return format("hello:%s", data.get());
                      })
                      .then("anything", v -> true)
              )),
          named("ForEach2",
              forEachOf(
                  asList("world1", "world2", "world3")
              ).perform(
                  ($, i) -> $.sequential(
                      $.simple("nothing", () -> {
                      }),
                      $.simple("sink1", () -> {
                      }),
                      $.simple("sink2", () -> {
                      })
                  )
              ))
      ));
    }

    @Test
    public void givenComplicatedTestAction$whenPerformed$thenWorksFine() {
      List<String> out = new LinkedList<>();
      Action action = composeAction(out);
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

  public static class WithResultVariationTest extends TestUtils.TestBase implements Context {
    @Test
    public void givenForEachWithTag$whenPerformed$thenResultPrinted() {
      final TestUtils.Out out1 = new TestUtils.Out();
      Action action = forEachOf(
          asList("A", "B")
      ).perform(
          ($, i) -> $.sequential(
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
              hasItemAt(0, Matchers.allOf(containsString("[o]"), containsString("ForEach"))),
              hasItemAt(1, Matchers.allOf(containsString("[oo]"), containsString("Sequential"))),
              hasItemAt(2, Matchers.allOf(containsString("[oo]"), containsString("+0"))),
              hasItemAt(3, Matchers.allOf(containsString("[oo]"), containsString("+1")))
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
              containsString("[o]"),
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
                containsString("[x]"),
                containsString("Retry(60[seconds]x1times)")
            ));
        assertThat(
            out.get(1),
            allOf(
                containsString("[x]"),
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
                  "contains('[o]')", (String u) -> u.contains("[o]")
              ),
              TestUtils.<TestUtils.Out, String>matcherBuilder().transform(
                  "get(1)", (TestUtils.Out v) -> v.get(1)
              ).check(
                  "contains('Retry(1[milliseconds]x1times')", (String u) -> u.contains("Retry(1[milliseconds]x1times")
              ),
              TestUtils.<TestUtils.Out, String>matcherBuilder().transform(
                  "get(2)", (TestUtils.Out v) -> v.get(2)
              ).check(
                  "contains('[xo]')", (String u) -> u.contains("[xo]")
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
              containsString("[o]"),
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
