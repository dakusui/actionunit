package com.github.dakusui.actionunit.compat;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.compat.connectors.Connectors;
import com.github.dakusui.actionunit.compat.connectors.Sink;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.ut.TestOutput;
import com.github.dakusui.actionunit.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import org.junit.ComparisonFailure;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import static com.github.dakusui.actionunit.helpers.Actions.*;
import static com.github.dakusui.actionunit.helpers.Utils.describe;
import static com.github.dakusui.actionunit.actions.ForEach.Mode.CONCURRENTLY;
import static com.github.dakusui.actionunit.actions.ForEach.Mode.SEQUENTIALLY;
import static com.github.dakusui.actionunit.ut.ActionsTest.autocloseableList;
import static com.github.dakusui.actionunit.utils.TestUtils.hasItemAt;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

@RunWith(Enclosed.class)
public class CompatActionsTest {
  public static class ForEach extends TestUtils.TestBase {
    @Test(timeout = 3000000)
    public void forEachTest() {
      final List<String> arr = new ArrayList<>();
      CompatActions.foreach(
          asList("1", "2"),
          new Sink.Base<String>("print") {
            @Override
            public void apply(String input, Object... outer) {
              arr.add(String.format("Hello %s", input));
            }
          }
      ).accept(new ActionRunner.Impl());
      assertArrayEquals(new Object[] { "Hello 1", "Hello 2" }, arr.toArray());
    }

    @Test
    public void givenForEachActionViaNonCollection$whenDescribe$thenLooksNice() {
      assertEquals(
          "CompatForEach (Sequential, ? items) {empty!}",
          describe(CompatActions.foreach(
              new Iterable<String>() {
                @Override
                public Iterator<String> iterator() {
                  return asList("hello", "world").iterator();
                }
              },
              SEQUENTIALLY,
              new Sink.Base<String>("empty!") {
                @Override
                public void apply(String s, Object... outer) {
                }
              }
          ))
      );
    }

    @Test
    public void givenForEachCreatedWithoutExplicitMode$whenPerform$thenWorksFine() {
      final List<String> arr = new ArrayList<>();
      CompatActions.foreach(
          asList("1", "2"),
          sequential(
              CompatActions.simple(new Runnable() {
                       @Override
                       public void run() {
                         arr.add("Hello!");
                       }
                     }
              ),
              CompatActions.tag(0)
          ),
          new Sink.Base<String>("print") {
            @Override
            public void apply(String input, Object... outer) {
              arr.add(String.format("Hello %s", input));
            }
          }
      ).accept(new ActionRunner.Impl());
      assertArrayEquals(new Object[] { "Hello!", "Hello 1", "Hello!", "Hello 2" }, arr.toArray());
    }

    @Test
    public void givenForEachActionWithAutoCloseableDataSource$whenPerformedConcurrently$thenClosedLooksNice() {
      String[] data = { "hello1", "hello2", "hello3", "hello4", "hello5" };
      final TestUtils.Out out = new TestUtils.Out();
      Action action = CompatActions.foreach(
          autocloseableList(out, "closed", data),
          CONCURRENTLY,
          new Sink.Base<String>() {
            @Override
            public void apply(String s, Object... outer) {
              out.writeLine(s);
            }
          }
      );
      action.accept(new ActionRunner.Impl(data.length - 1));

      assertThat(
          out,
          allOf(
              hasItem(equalTo("hello5")),
              hasItem(equalTo("hello2")),
              hasItem(equalTo("hello3")),
              hasItem(equalTo("hello4")),
              hasItem(equalTo("hello5")),
              hasItemAt(5, equalTo("closed"))
          ));
      assertEquals(6, out.size());
    }

    @Test
    public void givenForEachActionWithEmptyAutoCloseableDataSource$whenPerformedConcurrently$thenClosedLooksNice() {
      String[] data = {};
      final TestUtils.Out out = new TestUtils.Out();
      Action action = CompatActions.foreach(
          autocloseableList(out, "closed", data),
          CONCURRENTLY,
          new Sink.Base<String>() {
            @Override
            public void apply(String s, Object... outer) {
              out.writeLine(s);
            }
          }
      );
      action.accept(new ActionRunner.Impl());

      assertThat(out, hasItemAt(0, equalTo("closed")));
      assertEquals(1, out.size());
    }

    @Test
    public void givenConcurrentForEachActionWithAutoCloseableDataSource$whenPerformed$thenClosedLooksNice() {
      final TestUtils.Out out = new TestUtils.Out();
      Action action = CompatActions.foreach(
          autocloseableList(out, "closed", "hello", "world"),
          CONCURRENTLY,
          new Sink.Base<String>() {
            @Override
            public void apply(String s, Object... outer) {
              out.writeLine(s);
            }
          }
      );
      action.accept(new ActionRunner.Impl());

      assertThat(
          out,
          allOf(
              anyOf(
                  hasItemAt(0, equalTo("hello")),
                  hasItemAt(0, equalTo("world"))
              ),
              anyOf(
                  hasItemAt(1, equalTo("hello")),
                  hasItemAt(1, equalTo("world"))
              ),
              hasItemAt(2, equalTo("closed"))));
    }

    @Test
    public void givenForEachAction$whenDescribe$thenLooksNice() {
      assertEquals(
          "CompatForEach (Concurrent, 2 items) {(noname)}",
          describe(CompatActions.foreach(
              asList("hello", "world"),
              CONCURRENTLY,
              new Sink.Base<String>() {
                @Override
                public void apply(String s, Object... outer) {
                }
              }
          ))
      );
    }

    @Test
    public void givenForEachActionWithAutoCloseableDataSource$whenPerformed$thenClosedLooksNice() {
      final TestUtils.Out out = new TestUtils.Out();
      Action action = CompatActions.foreach(
          autocloseableList(out, "closed", "hello", "world"),
          SEQUENTIALLY,
          new Sink.Base<String>() {
            @Override
            public void apply(String s, Object... outer) {
              out.writeLine(s);
            }
          }
      );
      action.accept(new CompatActionRunnerWithResult());

      assertThat(
          out,
          allOf(
              hasItemAt(0, equalTo("hello")),
              hasItemAt(1, equalTo("world")),
              hasItemAt(2, equalTo("closed"))
          ));
    }
  }

  public static class Legacy extends TestUtils.TestBase {
    @Test
    public void givenWithAction$whenPerformed$thenWorksFine() {
      final List<String> arr = new ArrayList<>();
      CompatActions.with("world",
          new Sink.Base<String>() {
            @Override
            public void apply(String input, Object... outer) {
              arr.add(String.format("Hello, %s.", input));
              arr.add(String.format("%s, bye.", input));
            }
          }
      ).accept(new ActionRunner.Impl());
      assertEquals(asList("Hello, world.", "world, bye."), arr);
    }

    /**
     * Even if too many blocks are given, ActionUnit's normal runner doesn't report
     * an error. It's left to ActionValidator, which is not yet implemented as of
     * Aug/12/2016.
     */
    @Test
    public void givenActionWithInsufficientTags$whenPerformed$thenWorksFine() {
      CompatActions.with("world",
          sequential(nop()),
          new Sink.Base<String>() {
            @Override
            public void apply(String input, Object... outer) {
            }
          }
      ).accept(new ActionRunner.Impl());
    }

    @Test(expected = IllegalStateException.class)
    public void givenActionWithTooManyTags$whenPerformed$thenAppropriateErrorReported() {
      CompatActions.with("world",
          sequential(CompatActions.tag(0), CompatActions.tag(1)),
          new Sink.Base<String>() {
            @Override
            public void apply(String input, Object... outer) {
            }
          }
      ).accept(new ActionRunner.Impl());
    }

    @Test
    public void givenCompatAttemptAction$whenExceptionCaught$worksFine() {
      final List<String> out = new LinkedList<>();
      CompatActions.attempt(new Runnable() {
        @Override
        public void run() {
          out.add("try");
          throw new ActionException("thrown");
        }
      }).recover(/*you can omit exception class parameter you are going to catch ActionException*/ new Sink<ActionException>() {
        @Override
        public void apply(ActionException input, Context context) {
          out.add("catch");
          out.add(input.getMessage());
        }
      }).ensure(new Runnable() {
        @Override
        public void run() {
          out.add("finally");
        }
      }).build().accept(new ActionRunner.Impl());
      assertEquals(asList("try", "catch", "thrown", "finally"), out);
    }

    @Test
    public void givenPipeAction$whenPerformed$thenWorksFine() {
      CompatActions.with("world",
          CompatActions.pipe(
              Connectors.<String>context(),
              s -> new TestOutput.Text("hello:" + s), new Sink<TestOutput.Text>() {
                @Override
                public void apply(TestOutput.Text input, Context context) {
                  assertEquals("hello:world", input.value());
                }
              })).accept(new ActionRunner.Impl());
    }

    @Test
    public void givenSimplePipeAction$whenPerformed$thenWorksFine() {
      final TestUtils.Out out = new TestUtils.Out();
      Action action = CompatActions.foreach(asList("world", "WORLD", "test", "hello"),
          CompatActions.pipe(
              (Function<String, TestOutput.Text>) s -> {
                System.out.println("func:" + s);
                return new TestOutput.Text("hello:" + s);
              },
              new Sink<TestOutput.Text>() {
                @Override
                public void apply(TestOutput.Text input, Context context) {
                  System.out.println("sink:" + input);
                  out.writeLine(input.toString());
                }
              }
          ));
      CompatActionRunnerWithResult runner = new CompatActionRunnerWithResult();
      try {
        action.accept(runner);
      } finally {
        action.accept(runner.createPrinter());
      }
      assertThat(
          out,
          allOf(
              hasItemAt(0, equalTo("hello:world")),
              hasItemAt(1, equalTo("hello:WORLD")),
              hasItemAt(2, equalTo("hello:test")),
              hasItemAt(3, equalTo("hello:hello"))
          )
      );
    }

    @Test
    public void givenTestActionWithName$whenBuiltAndPerformed$thenWorksFine() {
      //noinspection unchecked
      CompatActions.<String, String>test()
          .given("Hello")
          .when(new Function<String, String>() {
            @Override
            public String apply(String input) {
              return "*" + input + "*";
            }
          })
          .then(
              allOf(
                  containsString("Hello"),
                  not(equalTo("Hello"))
              ))
          .build()
          .accept(new ActionRunner.Impl());
    }

    @Test(expected = ComparisonFailure.class)
    public void givenPipeAction$whenPerformedAndThrowsException$thenPassesThrough
        () throws Throwable {
      CompatActions.with("world",
          CompatActions.pipe(
              Connectors.<String>context(),
              new Function<String, TestOutput.Text>() {
                @Override
                public TestOutput.Text apply(String s) {
                  return new TestOutput.Text("Hello:" + s);
                }
              }, new Sink<TestOutput.Text>() {
                @Override
                public void apply(TestOutput.Text input, Context context) {
                  assertEquals("hello:world", input.value());
                }
              })).accept(new ActionRunner.Impl());
    }

    @Test
    public void givenPipeActionFromFunction$whenPerformed$thenWorksFine() throws
        Throwable {
      final List<String> out = new LinkedList<>();
      CompatActions.with("world",
          CompatActions.pipe(
              Connectors.<String>context(),
              new Function<String, TestOutput.Text>() {
                @Override
                public TestOutput.Text apply(String s) {
                  return new TestOutput.Text("Hello:" + s);
                }
              },
              new Sink<TestOutput.Text>() {
                @Override
                public void apply(TestOutput.Text input, Context context) {
                  out.add(input.value());
                }
              },
              new Sink<TestOutput.Text>() {
                @Override
                public void apply(TestOutput.Text input, Context context) {
                  out.add(input.value());
                }
              }
          )).accept(new ActionRunner.Impl());
      assertEquals(
          asList("Hello:world", "Hello:world"),
          out
      );
    }

    @Test(expected = IllegalArgumentException.class)
    public void givenAttemptAction$whenExceptionNotCaught$worksFine() {
      final List<String> out = new LinkedList<>();
      try {
        CompatActions.attempt(new Runnable() {
          @Override
          public void run() {
            out.add("try");
            throw new IllegalArgumentException("thrown");
          }
        }).recover(NullPointerException.class, new Sink<NullPointerException>() {
          @Override
          public void apply(NullPointerException input, Context context) {
            out.add("catch");
            out.add(input.getMessage());
          }
        }).ensure(new Runnable() {
          @Override
          public void run() {
            out.add("finally");
          }
        }).build().accept(new ActionRunner.Impl());
      } catch (IllegalArgumentException e) {
        assertEquals(asList("try", "finally"), out);
        throw e;
      }
    }

    @Test
    public void givenSimplestPipeAction$whenPerformed$thenWorksFine() {
      final List<TestOutput.Text> out = new LinkedList<>();
      CompatActions.with("world",
          CompatActions.pipe(
              new Function<String, TestOutput.Text>() {
                @Override
                public TestOutput.Text apply(String s) {
                  out.add(new TestOutput.Text("hello:" + s));
                  return out.get(0);
                }
              }
          )).accept(new ActionRunner.Impl());
      assertEquals("hello:world", out.get(0).value());
    }

  }
}
