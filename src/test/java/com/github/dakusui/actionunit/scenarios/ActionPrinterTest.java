package com.github.dakusui.actionunit.scenarios;

import com.github.dakusui.actionunit.actions.Composite;
import com.github.dakusui.actionunit.actions.Retry;
import com.github.dakusui.actionunit.ut.utils.TestUtils;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.ContextConsumer;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.visitors.ActionPrinter;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import com.github.dakusui.crest.Crest;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.Formatter;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.github.dakusui.actionunit.ut.utils.TestUtils.size;
import static com.github.dakusui.actionunit.core.ActionSupport.*;
import static com.github.dakusui.crest.Crest.asInteger;
import static com.github.dakusui.crest.Crest.asString;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
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
                          () -> Stream.of("hello1", "hello2", "hello3")
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
      Crest.assertThat(
          i,
          Crest.allOf(
              asString("next").containsString("Concurrent (top level)").$(),
              asString("next").containsString("do parallelly").$(),
              asString("next").containsString("Sequential (1st child)").$(),
              asString("next").containsString("do sequentially").$(),
              asString("next").containsString("simple1").$(),
              asString("next").containsString("(noname)").$(),
              asString("next").containsString("simple2").$(),
              asString("next").containsString("(noname)").$(),
              asString("next").containsString("simple3").$(),
              asString("next").containsString("(noname)").$(),
              asString("next").containsString("for each of data sequentially").$(),
              asString("next").containsString("(nop)").$()
          )
      );
      assertEquals(12, size(writer));
    }
  }

  @RunWith(Enclosed.class)
  public static class StdOutErrTest extends ActionComposer {
    @Test
    public void givenStdout$whenTestActionAccepts$thenNoErrorWillBeGiven() {
      composeAction().accept(new ActionPrinter(Writer.Std.OUT));
    }

    @Test
    public void givenStderr$whenTestActionAccepts$thenNoErrorWillBeGiven() {
      composeAction().accept(new ActionPrinter(Writer.Std.ERR));
    }

    public static class WithResultVariationTest extends TestUtils.ContextTestBase {
      @Test
      public void givenForEachWithTag$whenPerformed$thenResultPrinted() {
        final TestUtils.Out out1 = new TestUtils.Out();
        Action action = forEach(
            "i",
            () -> Stream.of("A", "B")
        ).perform(
            sequential(
                simple("+0", (c) -> out1.writeLine(c.valueOf("i") + "0")),
                simple("+1", (c) -> out1.writeLine(c.valueOf("i") + "1"))
            )
        );
        action.accept(TestUtils.createActionPerformer());
        assertEquals(asList("A0", "A1", "B0", "B1"), out1);

        final TestUtils.Out out2 = new TestUtils.Out();
        runAndReport(action, out2);
        Crest.assertThat(
            out2,
            Crest.allOf(
                asString("get", 0).containsString("[o]").containsString("for each").$(),
                asString("get", 1).containsString("[oo]").containsString("sequential").$(),
                asString("get", 2).containsString("[oo]").containsString("+0").$(),
                asString("get", 3).containsString("[oo]").containsString("(noname)").$(),
                asString("get", 4).containsString("[oo]").containsString("+1").$(),
                asString("get", 5).containsString("[oo]").containsString("(noname)").$(),
                asInteger("size").equalTo(6).$()
            ));
      }

      @Test
      public void givenRetryAction$whenPerformed$thenResultPrinted() {
        Action action = retry(nop()).times(1).withIntervalOf(1, TimeUnit.MINUTES).build();
        TestUtils.Out out = new TestUtils.Out();
        runAndReport(action, out);
        Crest.assertThat(
            out.get(0),
            Crest.allOf(
                asString().containsString("[o]").$(),
                asString().containsString("retry once in 60[seconds]").$()
            ));
      }

      @Test(expected = IllegalStateException.class, timeout = 10_000)
      public void givenFailingRetryAction$whenPerformed$thenResultPrinted() {
        final TestUtils.Out out = new TestUtils.Out();
        Action action = retry(simple("AlwaysFail", new ContextConsumer() {
          @Override
          public void accept(Context context) {
            throw new IllegalStateException(this.toString());
          }
        })).times(
            1
        ).withIntervalOf(
            1, TimeUnit.MILLISECONDS
        ).on(
            IllegalStateException.class
        ).$();
        runAndReport(action, out);
        Crest.assertThat(
            out,
            Crest.allOf(
                asString("get", 0).containsString("[E]").$(),
                asString("get", 0).containsString("retry once in 1[milliseconds] on IllegalStateException").$(),
                asString("get", 1).containsString("[EE]").$(),
                asString("get", 1).containsString("AlwaysFail").$()
            ));
      }

      @Test
      public void givenPassAfterRetryAction$whenPerformed$thenResultPrinted() {
        final TestUtils.Out outForRun = new TestUtils.Out();
        Retry.Builder retry = retry(
            simple("PassAfterFail", new ContextConsumer() {
              boolean tried = false;

              @Override
              public void accept(Context context) {
                try {
                  if (!tried) {
                    outForRun.writeLine("PassAfterFail");
                    throw new ActionException(this.toString());
                  }
                } finally {
                  tried = true;
                }
              }
            }));
        retry.times(1);
        retry.withIntervalOf(1, MILLISECONDS);
        Action action = retry.build();
        runAndReport(action, outForRun);
        Crest.assertThat(
            outForRun,
            Crest.allOf(
                asString("get", 0).containsString("PassAfterFail").$(),
                asString("get", 1).containsString("[o]").$(),
                asString("get", 1).containsString("retry once in 1[milliseconds]").$(),
                asString("get", 2).containsString("[Eo]").$(),
                asString("get", 2).containsString("PassAfterFail").$()
            )
        );
      }

      @Test
      public void givenTimeoutAction$whenPerformed$thenResultPrinted() {
        Action action = timeout(nop()).in(1, TimeUnit.MINUTES);
        final TestUtils.Out out = new TestUtils.Out();
        runAndReport(action, out);
        Crest.assertThat(
            out.get(0),
            Crest.allOf(
                Crest.asString().containsString("[o]").$(),
                Crest.asString().containsString("timeout in 60[seconds]").$()
            ));
      }

      @Test
      public void givenUnsupportedCompositeAction$whenPerformed$thenExceptionThrown() {
        Action action = new Composite.Impl(Collections.emptyList(), false) {
          @Override
          public void accept(Action.Visitor visitor) {
            visitor.visit(this);
          }
        };
        TestUtils.createReportingActionPerformer().performAndReport(action);
      }

      @Test(expected = UnsupportedOperationException.class)
      public void givenUnsupportedSimpleAction$whenPerformed$thenExceptionThrown() {
        Action action = new Action() {
          @Override
          public void formatTo(Formatter formatter, int flags, int width, int precision) {
            formatter.format("anonymous");
          }

          @Override
          public void accept(Action.Visitor visitor) {
            visitor.visit(this);
          }
        };
        TestUtils.createReportingActionPerformer().performAndReport(action);
      }

    }

    private static void runAndReport(Action action, TestUtils.Out out) {
      ReportingActionPerformer.create(out).performAndReport(action);
    }
  }
}
