package com.github.dakusui.actionunit.scenarios;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.Context;
import com.github.dakusui.actionunit.actions.ActionBase;
import com.github.dakusui.actionunit.actions.Composite;
import com.github.dakusui.actionunit.actions.TestAction;
import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.ActionPrinter;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.github.dakusui.actionunit.Actions.*;
import static com.github.dakusui.actionunit.connectors.Connectors.toSink;
import static com.github.dakusui.actionunit.scenarios.ActionPrinterTest.ImplTest.composeAction;
import static com.github.dakusui.actionunit.utils.TestUtils.hasItemAt;
import static com.google.common.collect.Iterables.size;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;

@RunWith(Enclosed.class)
public class ActionPrinterTest {

  public static class ImplTest {
    static Action composeAction() {
      return concurrent("Concurrent (top level)",
          sequential("Sequential (1st child)",
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
          foreach(
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
      composeAction().accept(ActionPrinter.Factory.trace());
    }

    @Test
    public void givenDebug$whenTestActionAccepts$thenNoErrorWillBeGiven() {
      composeAction().accept(ActionPrinter.Factory.debug());
    }

    @Test
    public void givenInfo$whenTestActionAccepts$thenNoErrorWillBeGiven() {
      composeAction().accept(ActionPrinter.Factory.info());
    }

    @Test
    public void givenWarn() {
      composeAction().accept(ActionPrinter.Factory.warn());
    }

    @Test
    public void givenError() {
      composeAction().accept(ActionPrinter.Factory.error());
    }

    @Test
    public void givenNew() {
      ActionPrinter<ActionPrinter.Writer> printer = ActionPrinter.Factory.create();
      composeAction().accept(printer);
      ActionPrinter.Writer.Impl writer = (ActionPrinter.Writer.Impl) printer.getWriter();
      Iterator<String> i = writer.iterator();
      assertThat(i.next(), containsString("Concurrent (top level)"));
      assertThat(i.next(), containsString("Concurrent"));
      assertThat(i.next(), containsString("Sequential (1st child)"));
      assertThat(i.next(), containsString("Sequential"));
      assertThat(i.next(), containsString("simple1"));
      i.next();
      assertThat(i.next(), containsString("simple2"));
      i.next();
      assertThat(i.next(), containsString("simple3"));
      i.next();
      assertThat(i.next(), containsString("ForEach"));
      assertEquals(13, size(writer));
    }
  }

  public static class StdOutErrTest extends TestUtils.TestBase {
    @Test
    public void givenStdout$whenTestActionAccepts$thenNoErrorWillBeGiven() {
      composeAction().accept(ActionPrinter.Factory.stdout());
    }

    @Test
    public void givenStderr$whenTestActionAccepts$thenNoErrorWillBeGiven() {
      composeAction().accept(ActionPrinter.Factory.stderr());
    }
  }

  public static class WithResultTest extends TestUtils.TestBase {
    private static Action composeAction(final List<String> out) {
      //noinspection unchecked
      return concurrent("Concurrent (top level)",
          sequential("Sequential (1st child)",
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
          foreach(
              asList("hello1", "hello2", "hello3"),
              new TestAction.Builder<String, Object>("ExampleTest")
                  .when(input -> {
                    out.add(format("hello:%s", input));
                    return format("hello:%s", input);
                  })
                  .then(anything()).build()
          ),
          foreach(
              asList("world1", "world2", "world3"),
              sequential(
                  simple(new Runnable() {
                    @Override
                    public void run() {

                    }
                  }),
                  tag(0)
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
      ActionRunner.WithResult runner = new ActionRunner.WithResult();
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
      ActionPrinter<ActionPrinter.Writer> printer = ActionPrinter.Factory.create();
      composeAction(out).accept(printer);
      ActionPrinter.Writer.Impl writer = (ActionPrinter.Writer.Impl) printer.<ActionPrinter.Writer.Impl>getWriter();
      Iterator<String> i = writer.iterator();
      assertThat(i.next(), containsString("Concurrent (top level)"));
      assertThat(i.next(), containsString("Concurrent"));
      assertThat(i.next(), containsString("Sequential (1st child)"));
      assertThat(i.next(), containsString("Sequential"));
      assertThat(i.next(), containsString("simple1"));
      i.next();
      assertThat(i.next(), containsString("simple2"));
      i.next();
      assertThat(i.next(), containsString("simple3"));
      i.next();
      assertThat(i.next(), containsString("ForEach"));
      assertThat(i.next(), containsString("ExampleTest"));
      assertThat(i.next(), containsString("Given"));
      assertThat(i.next(), containsString("When"));
      assertThat(i.next(), containsString("Then"));
      assertThat(i.next(), containsString("Sequential"));
      assertThat(i.next(), containsString("Sequential"));
      i.next();
      assertThat(i.next(), containsString("Tag(0)"));
      assertEquals(19, size(writer));
    }
  }

  public static class WithResultVariationTest extends TestUtils.TestBase {
    @Test
    public void givenForEachWithTag$whenPerformed$thenResultPrinted() {
      final TestUtils.Out out1 = new TestUtils.Out();
      Action action = foreach(asList("A", "B"), sequential(tag(0), tag(1)), new Sink<String>() {
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
      ActionRunner.WithResult runner = new ActionRunner.WithResult();
      action.accept(runner);
      assertEquals(asList("A0", "A1", "B0", "B1"), out1);

      final TestUtils.Out out2 = new TestUtils.Out();
      action.accept(runner.createPrinter(out2));

      assertThat(out2, allOf(
          hasItemAt(0, containsString("(+)ForEach")),
          hasItemAt(1, containsString("(+)Sequential")),
          hasItemAt(2, containsString("(+)Tag(0)")),
          hasItemAt(3, containsString("(+)Tag(1)"))
      ));
      Assert.assertThat(out2.size(), equalTo(4));
    }

    @Test
    public void givenRetryAction$whenPerformed$thenResultPrinted() {
      Action action = retry(nop(), 1, 1, TimeUnit.MINUTES);
      ActionRunner.WithResult runner = new ActionRunner.WithResult();
      action.accept(runner);
      final TestUtils.Out out = new TestUtils.Out();
      action.accept(runner.createPrinter(out));
      assertEquals("(+)Retry(60[seconds]x1times)", out.get(0));
    }

    @Test(expected = IllegalStateException.class)
    public void givenFailingRetryAction$whenPerformed$thenResultPrinted() {
      Action action = retry(simple(new Runnable() {
        @Override
        public void run() {
          throw new IllegalStateException(this.toString());
        }

        public String toString() {
          return "AlwaysFail";
        }
      }), 1, 1, TimeUnit.MINUTES);
      ActionRunner.WithResult runner = new ActionRunner.WithResult();
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
      Action action = retry(
          simple(new Runnable() {
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
      ActionRunner.WithResult runner = new ActionRunner.WithResult();
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
      Action action = timeout(nop(), 1, TimeUnit.MINUTES);
      ActionRunner.WithResult runner = new ActionRunner.WithResult();
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
      ActionRunner.WithResult runner = new ActionRunner.WithResult();
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
      ActionRunner.WithResult runner = new ActionRunner.WithResult();
      action.accept(runner);
    }

    @Test
    public void test() {
      final TestUtils.Out out = new TestUtils.Out();
      Action action = with("Hello", toSink(input -> {
        out.writeLine(input + " applied");
        return true;
      }));
      ActionRunner.WithResult runner = new ActionRunner.WithResult();
      action.accept(runner);
      action.accept(runner.createPrinter(out));
      assertEquals("Hello applied", out.get(0));
    }
  }
}
