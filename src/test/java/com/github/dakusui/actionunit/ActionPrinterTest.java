package com.github.dakusui.actionunit;

import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.actionunit.visitors.ActionPrinter;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import com.google.common.base.Function;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Iterator;

import static com.github.dakusui.actionunit.Actions.*;
import static com.google.common.collect.Iterables.size;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;

@RunWith(Enclosed.class)
public class ActionPrinterTest {

  public static class ImplTest {
    private static Action composeAction() {
      return concurrent("Concurrent",
          sequential("Sequential",
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
    public void givenStdout$whenTestActionAccepts$thenNoErrorWillBeGiven() {
      PrintStream stdout = System.out;
      System.setOut(new PrintStream(new OutputStream() {
        @Override
        public void write(int b) throws IOException {
        }
      }));
      try {
        composeAction().accept(ActionPrinter.Factory.stdout());
      } finally {
        System.setOut(stdout);
      }
    }

    @Test
    public void givenStderr$whenTestActionAccepts$thenNoErrorWillBeGiven() {
      PrintStream stderr = System.err;
      System.setErr(new PrintStream(new OutputStream() {
        @Override
        public void write(int b) throws IOException {
        }
      }));
      try {
        composeAction().accept(ActionPrinter.Factory.stderr());
      } finally {
        System.setErr(stderr);
      }
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
      ActionPrinter<ActionPrinter.Writer.Impl> printer = ActionPrinter.Factory.create();
      composeAction().accept(printer);
      Iterator<String> i = printer.getWriter().iterator();
      assertThat(i.next(), containsString("Concurrent"));
      assertThat(i.next(), containsString("Sequential"));
      assertThat(i.next(), containsString("simple1"));
      assertThat(i.next(), containsString("simple2"));
      assertThat(i.next(), containsString("simple3"));
      assertThat(i.next(), containsString("ForEach"));
      assertEquals(8, size(printer.getWriter()));
    }
  }

  public static class WithResultTest {
    private static Action composeAction() {
      return concurrent("Concurrent",
          sequential("Sequential",
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
                            System.out.println(String.format("hello:%s", input));
                            return String.format("hello:%s", input);
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
    public void test() {
      ActionRunner.WithResult runner = new ActionRunner.WithResult();
      Action action = composeAction();
      action.accept(runner);
      action.accept(runner.createPrinter());
    }
  }
}
