package com.github.dakusui.actionunit.scenarios;

import com.github.dakusui.actionunit.actions.ActionBase;
import com.github.dakusui.actionunit.actions.Composite;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.helpers.Builders;
import com.github.dakusui.actionunit.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.ActionPrinter;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import com.github.dakusui.actionunit.visitors.ReportingActionRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.github.dakusui.actionunit.helpers.Actions.*;
import static com.github.dakusui.actionunit.helpers.Builders.*;
import static com.github.dakusui.actionunit.helpers.Utils.size;
import static com.github.dakusui.actionunit.scenarios.ActionPrinterTest.ImplTest.composeAction;
import static com.github.dakusui.actionunit.utils.TestUtils.hasItemAt;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;

@RunWith(Enclosed.class)
public class ActionPrinterTest {
  public static class ImplTest extends TestUtils.TestBase {
    static Action composeAction() {
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
                          i -> nop()
                      )
                  ))));
    }

    @Test
    public void givenTrace() {
      composeAction().accept(ActionPrinter.Factory.DEFAULT_INSTANCE.trace());
    }

    @Test
    public void givenDebug$whenTestActionAccepts$thenNoErrorWillBeGiven() {
      composeAction().accept(ActionPrinter.Factory.DEFAULT_INSTANCE.debug());
    }

    @Test
    public void givenInfo$whenTestActionAccepts$thenNoErrorWillBeGiven() {
      composeAction().accept(ActionPrinter.Factory.DEFAULT_INSTANCE.info());
    }

    @Test
    public void givenWarn() {
      composeAction().accept(ActionPrinter.Factory.DEFAULT_INSTANCE.warn());
    }

    @Test
    public void givenError() {
      composeAction().accept(ActionPrinter.Factory.DEFAULT_INSTANCE.error());
    }

    @Test
    public void givenNew() {
      ActionPrinter printer = ActionPrinter.Factory.DEFAULT_INSTANCE.create();
      composeAction().accept(printer);
      ReportingActionRunner.Writer.Impl writer = (ReportingActionRunner.Writer.Impl) ((ActionPrinter.Impl) printer).getWriter();
      Iterator<String> i = writer.iterator();
      assertThat(i.next(), containsString("Concurrent (top level)"));
      assertThat(i.next(), containsString("Concurrent"));
      assertThat(i.next(), containsString("Sequential (1st child)"));
      assertThat(i.next(), containsString("Sequential"));
      assertThat(i.next(), containsString("simple1"));
      assertThat(i.next(), containsString("simple2"));
      assertThat(i.next(), containsString("simple3"));
      assertThat(i.next(), containsString("CompatForEach"));
      assertEquals(10, size(writer));
    }
  }

  public static class StdOutErrTest extends TestUtils.TestBase {
    @Test
    public void givenStdout$whenTestActionAccepts$thenNoErrorWillBeGiven() {
      composeAction().accept(ActionPrinter.Factory.DEFAULT_INSTANCE.stdout());
    }

    @Test
    public void givenStderr$whenTestActionAccepts$thenNoErrorWillBeGiven() {
      composeAction().accept(ActionPrinter.Factory.DEFAULT_INSTANCE.stderr());
    }
  }

  public static class WithResultTest extends TestUtils.TestBase {
    private static Action composeAction(final List<String> out) {
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
          forEachOf(
              asList("hello1", "hello2", "hello3")
          ).perform(
              data -> Builders.given(
                  "ExampleTest", () -> "ExampleTest")
                  .when("Say 'hello'", input -> {
                    out.add(format("hello:%s", input));
                    return format("hello:%s", input);
                  })
                  .then("anything", v -> true)
          ),
          forEachOf(
              asList("world1", "world2", "world3")
          ).perform(
              i -> sequential(
                  simple("nothing", () -> {
                  }),
                  simple("sink1", () -> {
                  }),
                  simple("sink2", () -> {
                  })
              )
          )
      ));
    }

    @Test
    public void givenComplicatedTestAction$whenPerformed$thenWorksFine() {
      List<String> out = new LinkedList<>();
      Action action = composeAction(out);
      try {
        action.accept(new ActionRunner.Impl());
        assertEquals(asList("hello:hello1", "hello:hello2", "hello:hello3"), out);
      } finally {
        runAndReport(action, new TestUtils.Out());
      }
    }

    @Test
    public void givenComplicatedTestAction$whenPrinted$thenPrintedCorrectly() {
      List<String> out = new LinkedList<>();
      ActionPrinter printer = ActionPrinter.Factory.DEFAULT_INSTANCE.create();
      composeAction(out).accept(printer);
      composeAction(out).accept(new ActionPrinter.Impl(ReportingActionRunner.Writer.Std.OUT));
      ReportingActionRunner.Writer.Impl writer = (ReportingActionRunner.Writer.Impl) ((ActionPrinter.Impl) printer).<ReportingActionRunner.Writer.Impl>getWriter();
      Iterator<String> i = writer.iterator();
      assertThat(i.next(), containsString("Concurrent (top level)"));
      assertThat(i.next(), containsString("Concurrent"));
      assertThat(i.next(), containsString("Sequential (1st child)"));
      assertThat(i.next(), containsString("Sequential"));
      assertThat(i.next(), containsString("simple1"));
      assertThat(i.next(), containsString("simple2"));
      assertThat(i.next(), containsString("simple3"));
      assertThat(i.next(), containsString("CompatForEach"));
      assertThat(i.next(), containsString("ExampleTest"));
      assertThat(i.next(), containsString("Given"));
      assertThat(i.next(), containsString("When"));
      assertThat(i.next(), containsString("Then"));
      assertThat(i.next(), containsString("CompatForEach"));
      assertThat(i.next(), containsString("Sequential"));
      i.next();
      assertThat(i.next(), containsString("Tag(0)"));
      assertEquals(16, size(writer));
    }
  }

  public static class WithResultVariationTest extends TestUtils.TestBase {
    @Test
    public void givenForEachWithTag$whenPerformed$thenResultPrinted() {
      final TestUtils.Out out1 = new TestUtils.Out();
      Action action = forEachOf(asList("A", "B")).perform(
          i -> sequential(
              simple("+0", () -> {
                out1.writeLine(i.get() + "0");
              }),
              simple("+1", () -> {
                out1.writeLine(i.get() + "1");
              })
          )
      );
      action.accept(new ActionRunner.Impl(2));
      assertEquals(asList("A0", "A1", "B0", "B1"), out1);

      final TestUtils.Out out2 = new TestUtils.Out();
      runAndReport(action, out2);
      assertThat(out2, allOf(
          hasItemAt(0, containsString("(+)CompatForEach")),
          hasItemAt(1, containsString("(+)Sequential")),
          hasItemAt(2, containsString("(+)Tag(0)")),
          hasItemAt(3, containsString("(+)Tag(1)"))
      ));
      Assert.assertThat(
          out2.size(),
          equalTo(4)
      );
    }

    @Test
    public void givenRetryAction$whenPerformed$thenResultPrinted() {
      Action action = retry(nop()).times(1).withIntervalOf(1, TimeUnit.MINUTES);
      TestUtils.Out out = new TestUtils.Out();
      runAndReport(action, out);
      assertEquals("(+)Retry(60[seconds]x1times)", out.get(0));
    }

    @Test(expected = IllegalStateException.class)
    public void givenFailingRetryAction$whenPerformed$thenResultPrinted() {
      final TestUtils.Out out = new TestUtils.Out();
      Action action = retry(simple("AlwaysFail", new Runnable() {
        @Override
        public void run() {
          throw new IllegalStateException(this.toString());
        }
      })).times(1).withIntervalOf(1, TimeUnit.MINUTES);
      try {
        runAndReport(action, out);
      } finally {
        assertEquals("(E)Retry(60[seconds]x1times)", out.get(0));
        assertEquals("  (E)AlwaysFail", out.get(1));
      }
    }

    @Test
    public void givenPassAfterRetryAction$whenPerformed$thenResultPrinted() {
      final TestUtils.Out out = new TestUtils.Out();
      Action action = retry(
          simple("PassAfterFail", new Runnable() {
            boolean tried = false;

            @Override
            public void run() {
              try {
                if (!tried) {
                  out.writeLine(this.toString());
                  throw new ActionException(this.toString());
                }
              } finally {
                tried = true;
              }
            }
          })).times(1).withIntervalOf(
          1, MILLISECONDS);
      try {
        runAndReport(action, out);
      } finally {
        assertEquals("PassAfterFail", out.get(0));
        assertEquals("(+)Retry(1[milliseconds]x1times)", out.get(1));
        assertEquals("  (+)PassAfterFail; 2 times", out.get(2));
      }
    }

    @Test
    public void givenTimeoutAction$whenPerformed$thenResultPrinted() {
      Action action = timeout(nop()).in(1, TimeUnit.MINUTES);
      final TestUtils.Out out = new TestUtils.Out();
      runAndReport(action, out);
      assertEquals("(+)TimeOut(60[seconds])", out.get(0));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void givenUnsupportedCompositeAction$whenPerformed$thenExceptionThrown() {
      Action action = new Composite.Base("", Collections.<Action>emptyList()) {
        @Override
        public void accept(Visitor visitor) {
          visitor.visit(this);
        }
      };
      new ReportingActionRunner.Builder(action).to(ReportingActionRunner.Writer.Std.OUT).build().perform();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void givenUnsupportedSimpleAction$whenPerformed$thenExceptionThrown() {
      Action action = new ActionBase() {
        @Override
        public void accept(Visitor visitor) {
          visitor.visit(this);
        }
      };
      new ReportingActionRunner.Builder(action).to(ReportingActionRunner.Writer.Std.OUT).build().perform();
    }
  }

  private static void runAndReport(Action action, TestUtils.Out out) {
    new ReportingActionRunner.Builder(action).to(out).build().perform();
  }
}
