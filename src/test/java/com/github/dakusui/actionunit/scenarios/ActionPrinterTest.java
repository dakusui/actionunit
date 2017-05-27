package com.github.dakusui.actionunit.scenarios;

import com.github.dakusui.actionunit.compat.visitors.CompatActionRunnerWithResult;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.compat.CompatActions;
import com.github.dakusui.actionunit.compat.Context;
import com.github.dakusui.actionunit.actions.ActionBase;
import com.github.dakusui.actionunit.actions.Composite;
import com.github.dakusui.actionunit.compat.actions.CompatTestAction;
import com.github.dakusui.actionunit.compat.connectors.Sink;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.ActionPrinter;
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
import static com.github.dakusui.actionunit.helpers.Utils.size;
import static com.github.dakusui.actionunit.compat.connectors.Connectors.toSink;
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
      return CompatActions.concurrent("Concurrent (top level)",
          CompatActions.sequential("Sequential (1st child)",
              simple("simple1", new Runnable() {
                @Override
                public void run() {
                }
              }),
              simple("simple2", new Runnable() {
                @Override
                public void run() {
                }
              })
          ),
          simple("simple3", new Runnable() {
            @Override
            public void run() {
            }
          }),
          CompatActions.foreach(
              asList("hello1", "hello2", "hello3"),
              new Sink.Base<String>("block1") {
                @Override
                public void apply(String input, Object... outer) {
                }
              }
          )
      );
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
      return CompatActions.concurrent("Concurrent (top level)",
          CompatActions.sequential("Sequential (1st child)",
              simple("simple1", new Runnable() {
                @Override
                public void run() {
                }
              }),
              simple("simple2", new Runnable() {
                @Override
                public void run() {
                }
              })
          ),
          simple("simple3", new Runnable() {
            @Override
            public void run() {
            }
          }),
          CompatActions.foreach(
              asList("hello1", "hello2", "hello3"),
              new CompatTestAction.Builder<String, Object>("ExampleTest")
                  .when(input -> {
                    out.add(format("hello:%s", input));
                    return format("hello:%s", input);
                  })
                  .then(anything()).build()
          ),
          CompatActions.foreach(
              asList("world1", "world2", "world3"),
              sequential(
                  CompatActions.simple(new Runnable() {
                    @Override
                    public void run() {

                    }
                  }),
                  CompatActions.tag(0)
              ),
              new Sink<String>() {
                @Override
                public void apply(String input, Context context) {
                }

                @Override
                public String toString() {
                  return "sink1";
                }
              },
              new Sink<String>() {
                @Override
                public void apply(String input, Context context) {
                }

                @Override
                public String toString() {
                  return "sink2";
                }
              }
          )
      );
    }

    @Test
    public void givenComplicatedTestAction$whenPerformed$thenWorksFine() {
      List<String> out = new LinkedList<>();
      Action action = composeAction(out);
      CompatActionRunnerWithResult runner = new CompatActionRunnerWithResult();
      try {
        action.accept(runner);
        assertEquals(asList("hello:hello1", "hello:hello2", "hello:hello3"), out);
      } finally {
        action.accept(runner.createPrinter());
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
      Action action = CompatActions.foreach(asList("A", "B"), sequential(CompatActions.tag(0), CompatActions.tag(1)), new Sink<String>() {
            @Override
            public void apply(String input, Context context) {
              out1.writeLine(input + "0");
            }
          }, new Sink<String>() {
            @Override
            public void apply(String input, Context context) {
              out1.writeLine(input + "1");
            }
          }
      );
      CompatActionRunnerWithResult runner = new CompatActionRunnerWithResult();
      action.accept(runner);
      assertEquals(asList("A0", "A1", "B0", "B1"), out1);

      final TestUtils.Out out2 = new TestUtils.Out();
      action.accept(runner.createPrinter(out2));

      assertThat(out2, allOf(
          hasItemAt(0, containsString("(+)CompatForEach")),
          hasItemAt(1, containsString("(+)Sequential")),
          hasItemAt(2, containsString("(+)Tag(0)")),
          hasItemAt(3, containsString("(+)Tag(1)"))
      ));
      Assert.assertThat(out2.size(), equalTo(4));
    }

    @Test
    public void givenRetryAction$whenPerformed$thenResultPrinted() {
      Action action = CompatActions.retry(nop(), 1, 1, TimeUnit.MINUTES);
      CompatActionRunnerWithResult runner = new CompatActionRunnerWithResult();
      action.accept(runner);
      final TestUtils.Out out = new TestUtils.Out();
      action.accept(runner.createPrinter(out));
      assertEquals("(+)Retry(60[seconds]x1times)", out.get(0));
    }

    @Test(expected = IllegalStateException.class)
    public void givenFailingRetryAction$whenPerformed$thenResultPrinted() {
      Action action = CompatActions.retry(CompatActions.simple(new Runnable() {
        @Override
        public void run() {
          throw new IllegalStateException(this.toString());
        }

        public String toString() {
          return "AlwaysFail";
        }
      }), 1, 1, TimeUnit.MINUTES);
      CompatActionRunnerWithResult runner = new CompatActionRunnerWithResult();
      try {
        action.accept(runner);
      } finally {

        final TestUtils.Out out = new TestUtils.Out();
        action.accept(runner.createPrinter(out));
        assertEquals("(E)Retry(60[seconds]x1times)", out.get(0));
        assertEquals("  (E)AlwaysFail", out.get(1));
      }
    }

    @Test
    public void givenPassAfterRetryAction$whenPerformed$thenResultPrinted() {
      final TestUtils.Out out = new TestUtils.Out();
      Action action = CompatActions.retry(
          CompatActions.simple(new Runnable() {
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

            public String toString() {
              return "PassAfterFail";
            }
          }),
          1, // once
          1, MILLISECONDS);
      CompatActionRunnerWithResult runner = new CompatActionRunnerWithResult();
      try {
        action.accept(runner);
      } finally {
        action.accept(runner.createPrinter(out));
        assertEquals("PassAfterFail", out.get(0));
        assertEquals("(+)Retry(1[milliseconds]x1times)", out.get(1));
        assertEquals("  (+)PassAfterFail; 2 times", out.get(2));
      }
    }

    @Test
    public void givenTimeoutAction$whenPerformed$thenResultPrinted() {
      Action action = CompatActions.timeout(nop(), 1, TimeUnit.MINUTES);
      CompatActionRunnerWithResult runner = new CompatActionRunnerWithResult();
      action.accept(runner);
      final TestUtils.Out out = new TestUtils.Out();
      action.accept(runner.createPrinter(out));
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
      CompatActionRunnerWithResult runner = new CompatActionRunnerWithResult();
      action.accept(runner);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void givenUnsupportedSimpleAction$whenPerformed$thenExceptionThrown() {
      Action action = new ActionBase() {
        @Override
        public void accept(Visitor visitor) {
          visitor.visit(this);
        }
      };
      CompatActionRunnerWithResult runner = new CompatActionRunnerWithResult();
      action.accept(runner);
    }

    @Test
    public void test() {
      final TestUtils.Out out = new TestUtils.Out();
      Action action = CompatActions.with("Hello", toSink(input -> {
        out.writeLine(input + " applied");
        return true;
      }));
      CompatActionRunnerWithResult runner = new CompatActionRunnerWithResult();
      action.accept(runner);
      action.accept(runner.createPrinter(out));
      assertEquals("Hello applied", out.get(0));
    }
  }
}
