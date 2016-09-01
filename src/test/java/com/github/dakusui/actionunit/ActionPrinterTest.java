package com.github.dakusui.actionunit;

import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.actionunit.visitors.ActionPrinter;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.github.dakusui.actionunit.ActionPrinterTest.ImplTest.composeAction;
import static com.github.dakusui.actionunit.Actions.*;
import static com.github.dakusui.actionunit.connectors.Connectors.toSink;
import static com.google.common.collect.Iterables.size;
import static java.lang.String.format;
import static java.util.Arrays.asList;
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
          forEach(
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

  public static class StdOutErrTest extends TestUtils.StdOutTestBase {
    @Test
    public void givenStdout$whenTestActionAccepts$thenNoErrorWillBeGiven() {
      composeAction().accept(ActionPrinter.Factory.stdout());
    }

    @Test
    public void givenStderr$whenTestActionAccepts$thenNoErrorWillBeGiven() {
      composeAction().accept(ActionPrinter.Factory.stderr());
    }
  }

  public static class WithResultTest extends TestUtils.StdOutTestBase {
    private static Action composeAction(final List<String> out) {
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
          forEach(
              asList("hello1", "hello2", "hello3"),
              new TestAction.Builder<String, String>("ExampleTest")
                  .when(new Function<String, String>() {
                          @Override
                          public String apply(String input) {
                            out.add(format("hello:%s", input));
                            return format("hello:%s", input);
                          }
                        }
                  )
                  .then(CoreMatchers.<String>anything()).build()
          ),
          forEach(
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
      action.accept(runner);
      assertEquals(asList("hello:hello1", "hello:hello2", "hello:hello3"), out);

      action.accept(runner.createPrinter());
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
      //noinspection unchecked
      assertThat(i.next(), allOf(containsString("Given"), containsString("When"), containsString("Then")));
      assertThat(i.next(), containsString("Sequential"));
      assertThat(i.next(), containsString("Sequential"));
      i.next();
      assertThat(i.next(), containsString("Tag(0)"));
      assertEquals(16, size(writer));
    }
  }

  public static class WithResultVariationTest extends TestUtils.StdOutTestBase {
    @Test
    public void givenForEachWithTag$whenPerformed$thenResultPrinted() {
      final TestUtils.Out out1 = new TestUtils.Out();
      Action action = forEach(asList("A", "B"), sequential(tag(0), tag(1)), new Sink<String>() {
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
      Assert.assertThat(out2.get(0), containsString("(+)ForEach"));
      Assert.assertThat(out2.get(1), containsString("(-)Sequential"));
      Assert.assertThat(out2.get(2), containsString("(-)Tag(0)"));
      Assert.assertThat(out2.get(3), containsString("(-)Tag(1)"));
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
      Action action = new Action.Composite.Base("", Collections.<Action>emptyList()) {
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
      Action action = new Action.Base() {
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
      Action action = with("Hello", toSink(new Predicate<Object>() {
        @Override
        public boolean apply(Object input) {
          out.writeLine(input + " applied");
          return true;
        }
      }));
      ActionRunner.WithResult runner = new ActionRunner.WithResult();
      action.accept(runner);
      action.accept(runner.createPrinter(out));
      assertEquals("Hello applied", out.get(0));
    }
  }
}
