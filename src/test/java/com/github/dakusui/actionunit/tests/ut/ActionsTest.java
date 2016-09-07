package com.github.dakusui.actionunit.tests.ut;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.Actions;
import com.github.dakusui.actionunit.Context;
import com.github.dakusui.actionunit.actions.Composite;
import com.github.dakusui.actionunit.connectors.Connectors;
import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.actionunit.utils.Abort;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import com.google.common.base.Function;
import com.google.common.base.Predicates;
import org.junit.ComparisonFailure;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.github.dakusui.actionunit.Actions.*;
import static com.github.dakusui.actionunit.Utils.describe;
import static com.github.dakusui.actionunit.actions.ForEach.Mode.CONCURRENTLY;
import static com.github.dakusui.actionunit.actions.ForEach.Mode.SEQUENTIALLY;
import static com.github.dakusui.actionunit.exceptions.ActionException.wrap;
import static com.github.dakusui.actionunit.utils.TestUtils.hasItemAt;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.synchronizedList;
import static java.util.concurrent.TimeUnit.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ActionsTest {
  @Test
  public void simpleTest() {
    final List<String> arr = new ArrayList<>();
    simple(new Runnable() {
      @Override
      public void run() {
        arr.add("Hello");
      }
    }).accept(new ActionRunner.Impl());
    assertArrayEquals(arr.toArray(), new Object[] { "Hello" });
  }

  @Test
  public void sequentialTest() {
    final List<String> arr = new ArrayList<>();
    sequential(
        simple(new Runnable() {
          @Override
          public void run() {
            arr.add("Hello A");
          }
        }),
        simple(new Runnable() {
          @Override
          public void run() {
            arr.add("Hello B");
          }
        })
    ).accept(new ActionRunner.Impl());
    assertEquals(asList("Hello A", "Hello B"), arr);
  }

  @Test
  public void givenSequentialAction$whenSize$thenCorrect() {
    Composite action = (Composite) sequential(nop(), nop(), nop());
    assertEquals(3, action.size());
  }

  @Test
  public void givenSequentialAction$whenDescribe$thenLooksNice() {
    assertEquals("Sequential (1 actions)", describe(sequential(nop())));
  }

  @Test(timeout = 300000)
  public void concurrentTest() throws InterruptedException {
    final List<String> arr = synchronizedList(new ArrayList<String>());
    concurrent(
        simple(new Runnable() {
          @Override
          public void run() {
            arr.add("Hello A");
          }
        }),
        simple(new Runnable() {
          @Override
          public void run() {
            arr.add("Hello B");
          }
        })
    ).accept(new ActionRunner.Impl());
    Collections.sort(arr);
    assertEquals(asList("Hello A", "Hello B"), arr);
  }

  @Test(timeout = 300000)
  public void concurrentTest$checkConcurrency() throws InterruptedException {
    final List<Map.Entry<Long, Long>> arr = synchronizedList(new ArrayList<Map.Entry<Long, Long>>());
    try {
      concurrent(
          simple(new Runnable() {
            @Override
            public void run() {
              arr.add(createEntry());
            }
          }),
          simple(new Runnable() {
            @Override
            public void run() {
              arr.add(createEntry());
            }
          })
      ).accept(new ActionRunner.Impl());
    } finally {
      for (Map.Entry<Long, Long> i : arr) {
        for (Map.Entry<Long, Long> j : arr) {
          assertTrue(i.getValue() > j.getKey());
        }
      }
    }
  }

  private Map.Entry<Long, Long> createEntry() {
    long before = currentTimeMillis();
    try {
      TimeUnit.MILLISECONDS.sleep(100);
      return new AbstractMap.SimpleEntry<>(
          before,
          currentTimeMillis()
      );
    } catch (InterruptedException e) {
      throw wrap(e);
    }
  }

  @Test(timeout = 300000, expected = NullPointerException.class)
  public void concurrentTest$runtimeExceptionThrown() throws InterruptedException {
    final List<String> arr = synchronizedList(new ArrayList<String>());
    try {
      concurrent(
          simple(new Runnable() {
            @Override
            public void run() {
              arr.add("Hello A");
              throw new NullPointerException();
            }
          }),
          simple(new Runnable() {
            @Override
            public void run() {
              arr.add("Hello B");
            }
          })
      ).accept(new ActionRunner.Impl());
    } finally {
      Collections.sort(arr);
      assertEquals(asList("Hello A", "Hello B"), arr);
    }
  }

  @Test(timeout = 300000, expected = Error.class)
  public void concurrentTest$errorThrown() throws InterruptedException {
    final List<String> arr = synchronizedList(new ArrayList<String>());
    try {
      concurrent(
          simple(new Runnable() {
            @Override
            public void run() {
              arr.add("Hello A");
              throw new Error();
            }
          }),
          simple(new Runnable() {
            @Override
            public void run() {
              arr.add("Hello B");
            }
          })
      ).accept(new ActionRunner.Impl());
    } finally {
      Collections.sort(arr);
      assertEquals(asList("Hello A", "Hello B"), arr);
    }
  }

  @Test
  public void timeoutTest() {
    final List<String> arr = new ArrayList<>();
    timeout(simple(new Runnable() {
          @Override
          public void run() {
            arr.add("Hello");
          }
        }),
        ////
        // 10 msec should be sufficient to finish the action above.
        10, MILLISECONDS
    ).accept(new ActionRunner.Impl());
    assertArrayEquals(new Object[] { "Hello" }, arr.toArray());
  }

  @Test
  public void givenTimeoutAction$whenDescribe$thenLooksNice() {
    assertEquals("TimeOut(1[milliseconds])", describe(timeout(nop(), 1, MILLISECONDS)));
    assertEquals("TimeOut(10[seconds])", describe(timeout(nop(), 10000, MILLISECONDS)));
    assertEquals("TimeOut(1000[days])", describe(timeout(nop(), 1000, DAYS)));
  }

  @Test(expected = TimeoutException.class)
  public void timeoutTest$timeout() throws Throwable {
    final List<String> arr = new ArrayList<>();
    try {
      timeout(
          simple(new Runnable() {
            @Override
            public void run() {
              arr.add("Hello");
              try {
                TimeUnit.SECONDS.sleep(30);
              } catch (InterruptedException e) {
                throw wrap(e);
              }
            }
          }),
          1, MILLISECONDS
      ).accept(new ActionRunner.Impl());
    } catch (ActionException e) {
      throw e.getCause();
    } finally {
      assertArrayEquals(new Object[] { "Hello" }, arr.toArray());
    }
  }

  @Test(expected = RuntimeException.class, timeout = 300000)
  public void givenTimeOutAtTopLevel$whenRuntimeExceptionThrownFromInside$thenRuntimeException() throws Throwable {
    final List<String> arr = new ArrayList<>();
    try {
      timeout(simple(new Runnable() {
            @Override
            public void run() {
              arr.add("Hello");
              throw new RuntimeException();
            }
          }),
          100, MILLISECONDS
      ).accept(new ActionRunner.Impl());
    } catch (ActionException e) {
      throw e.getCause();
    } finally {
      assertArrayEquals(new Object[] { "Hello" }, arr.toArray());
    }
  }

  @Test(expected = Error.class, timeout = 300000)
  public void givenTimeOutAtTopLevel$whenErrorThrownFromInside$thenError() throws Throwable {
    final List<String> arr = new ArrayList<>();
    try {
      timeout(simple(new Runnable() {
            @Override
            public void run() {
              arr.add("Hello");
              throw new Error();
            }
          }),
          100, MILLISECONDS
      ).accept(new ActionRunner.Impl());
    } catch (ActionException e) {
      throw e.getCause();
    } finally {
      assertArrayEquals(new Object[] { "Hello" }, arr.toArray());
    }
  }

  @Test(timeout = 300000)
  public void retryTest() {
    final List<String> arr = new ArrayList<>();
    retry(simple(new Runnable() {
          @Override
          public void run() {
            arr.add("Hello");
          }
        }),
        0, 1, MILLISECONDS
    ).accept(new ActionRunner.Impl());
    assertArrayEquals(new Object[] { "Hello" }, arr.toArray());
  }

  @Test(timeout = 300000)
  public void retryTest$failOnce() {
    final List<String> arr = new ArrayList<>();
    try {
      retry(simple(new Runnable() {
            int i = 0;

            @Override
            public void run() {
              arr.add("Hello");
              if (i < 1) {
                i++;
                throw new ActionException("fail");
              }
            }
          }),
          1, 1, MILLISECONDS
      ).accept(new ActionRunner.Impl());
    } finally {
      assertArrayEquals(new Object[] { "Hello", "Hello" }, arr.toArray());
    }
  }

  @Test(expected = Abort.class)
  public void givenRetryAction$whenAbortException$thenAborted() {
    final TestUtils.Out out = new TestUtils.Out();
    try {
      retry(simple(new Runnable() {
            @Override
            public void run() {
              out.writeLine("run");
              throw Abort.abort();
            }
          }),
          2, 1, MILLISECONDS
      ).accept(new ActionRunner.Impl());
    } finally {
      assertThat(out, hasSize(1));
    }
  }

  @Test(expected = IOException.class)
  public void givenRetryAction$whenAbortException2$thenAbortedAndRootExceptionStoredProperly() throws Throwable {
    final TestUtils.Out out = new TestUtils.Out();
    try {
      retry(simple(new Runnable() {
            @Override
            public void run() {
              out.writeLine("Hello");
              throw Abort.abort(new IOException());
            }
          }),
          2, 1, MILLISECONDS
      ).accept(new ActionRunner.Impl());
    } catch (Abort e) {
      throw e.getCause();
    } finally {
      assertThat(out, hasSize(1));
    }
  }

  @Test(expected = ActionException.class, timeout = 300000)
  public void retryTest$failForever() {
    final List<String> arr = new ArrayList<>();
    try {
      retry(simple(new Runnable() {
            @Override
            public void run() {
              arr.add("Hello");
              throw new ActionException("fail");
            }
          }),
          1, 1, MILLISECONDS
      ).accept(new ActionRunner.Impl());
    } finally {
      assertArrayEquals(new Object[] { "Hello", "Hello" }, arr.toArray());
    }
  }

  @Test
  public void givenRetryAction$whenDescribe$thenLooksNice() {
    assertEquals("Retry(2[seconds]x1times)", describe(retry(nop(), 1, 2, SECONDS)));
  }

  @Test(timeout = 3000000)
  public void givenNothingForChildAction$whenWhilActionPerformedWithAlwaysFalseCondition$thenQuitImmediately() {
    Action action = repeatwhile(Predicates.alwaysFalse());
    action.accept(new ActionRunner.Impl());
  }

  @Test(timeout = 3000000)
  public void forEachTest() {
    final List<String> arr = new ArrayList<>();
    foreach(
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
  public void givenForEachAction$whenDescribe$thenLooksNice() {
    assertEquals(
        "ForEach (Concurrent, 2 items) {(noname)}",
        describe(foreach(
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
    Action action = foreach(
        autocloseableList(out, "closed", "hello", "world"),
        SEQUENTIALLY,
        new Sink.Base<String>() {
          @Override
          public void apply(String s, Object... outer) {
            out.writeLine(s);
          }
        }
    );
    action.accept(new ActionRunner.WithResult());

    assertThat(
        out,
        allOf(
            hasItemAt(0, equalTo("hello")),
            hasItemAt(1, equalTo("world")),
            hasItemAt(2, equalTo("closed"))
        ));
  }

  @Test
  public void givenForEachActionWithAutoCloseableDataSource$whenPerformedConcurrently$thenClosedLooksNice() {
    String[] data = { "hello1", "hello2", "hello3", "hello4", "hello5" };
    final TestUtils.Out out = new TestUtils.Out();
    Action action = foreach(
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
    Action action = foreach(
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
    Action action = foreach(
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

  @SafeVarargs
  private static <T> List<T> autocloseableList(final TestUtils.Out out, final String msg, final T... values) {
    return new AbstractList<T>() {
      public Iterator<T> iterator() {
        class I implements Iterator<T>, AutoCloseable {
          final Iterator<T> inner = asList(values).iterator();

          @Override
          public void close() throws Exception {
            out.writeLine(msg);
          }

          @Override
          public boolean hasNext() {
            return inner.hasNext();
          }

          @Override
          public T next() {
            return inner.next();
          }

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }
        }
        return new I();
      }

      @Override
      public T get(int index) {
        return values[index];
      }

      @Override
      public int size() {
        return values.length;
      }
    };
  }

  @Test
  public void givenForEachActionViaNonCollection$whenDescribe$thenLooksNice() {
    assertEquals(
        "ForEach (Sequential, ? items) {empty!}",
        describe(foreach(
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
    foreach(
        asList("1", "2"),
        sequential(
            simple(new Runnable() {
                     @Override
                     public void run() {
                       arr.add("Hello!");
                     }
                   }
            ),
            tag(0)
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
  public void givenWithAction$whenPerformed$thenWorksFine() {
    final List<String> arr = new ArrayList<>();
    with("world",
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
    with("world",
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
    with("world",
        sequential(tag(0), tag(1)),
        new Sink.Base<String>() {
          @Override
          public void apply(String input, Object... outer) {
          }
        }
    ).accept(new ActionRunner.Impl());
  }

  @Test
  public void nopTest() {
    // Just make sure no error happens
    Actions.nop().accept(new ActionRunner.Impl());
  }


  @Test(timeout = 3000000)
  public void givenSleepAction$whenPerform$thenExpectedAmountOfTimeSpent() {
    ////
    // To force JVM load classes used by this test, run the action once for warm-up.
    sleep(1, TimeUnit.MILLISECONDS).accept(new ActionRunner.Impl());
    ////
    // Let's do the test.
    long before = currentTimeMillis();
    sleep(1, TimeUnit.MILLISECONDS).accept(new ActionRunner.Impl());
    //noinspection unchecked
    assertThat(
        currentTimeMillis() - before,
        allOf(
            greaterThanOrEqualTo(1L),
            ////
            // Depending on unpredictable conditions, such as JVM's internal state,
            // GC, class loading, etc.,  "sleep" action may take a longer time
            // than 1 msec to perform. In this case I'm giving 3 msec including
            // grace period.
            lessThan(3L)
        )
    );
  }

  @Test
  public void givenSleep$whenToString$thenLooksGood() {
    assertEquals("sleep for 1[seconds]", sleep(1234, TimeUnit.MILLISECONDS).toString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void givenNegative$whenSleep$thenException() {
    sleep(-1, TimeUnit.MILLISECONDS);
  }

  @Test
  public void givenAttemptAction$whenExceptionCaught$worksFine() {
    final List<String> out = new LinkedList<>();
    attempt(new Runnable() {
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

  @Test(expected = RuntimeException.class)
  public void givenAttemptAction$whenExceptionNotCaught$worksFine() {
    final List<String> out = new LinkedList<>();
    try {
      attempt(new Runnable() {
        @Override
        public void run() {
          out.add("try");
          throw new RuntimeException("thrown");
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
    } finally {
      assertEquals(asList("try", "finally"), out);
    }
  }

  @Test
  public void givenPipeAction$whenPerformed$thenWorksFine() {
    with("world",
        pipe(
            Connectors.<String>context(),
            new Function<String, TestOutput.Text>() {
              @Override
              public TestOutput.Text apply(String s) {
                return new TestOutput.Text("hello:" + s);
              }
            }, new Sink<TestOutput.Text>() {
              @Override
              public void apply(TestOutput.Text input, Context context) {
                assertEquals("hello:world", input.value());
              }
            })).accept(new ActionRunner.Impl());
  }

  @Test
  public void givenSimplePipeAction$whenPerformed$thenWorksFine() {
    final TestUtils.Out out = new TestUtils.Out();
    Action action = foreach(asList("world", "WORLD", "test", "hello"),
        pipe(
            new Function<String, TestOutput.Text>() {
              @Override
              public TestOutput.Text apply(String s) {
                System.out.println("func:" + s);
                return new TestOutput.Text("hello:" + s);
              }
            },
            new Sink<TestOutput.Text>() {
              @Override
              public void apply(TestOutput.Text input, Context context) {
                System.out.println("sink:" + input);
                out.writeLine(input.toString());
              }
            }
        ));
    ActionRunner.WithResult runner = new ActionRunner.WithResult();
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
  public void givenSimplestPipeAction$whenPerformed$thenWorksFine() {
    final List<TestOutput.Text> out = new LinkedList<>();
    with("world",
        pipe(
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

  @Test
  public void givenTestAction$whenBuiltAndPerformed$thenWorksFine() {

  }

  @Test
  public void givenTestActionWithName$whenBuiltAndPerformed$thenWorksFine() {
    //noinspection unchecked
    Actions.<String, String>test()
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
    with("world",
        pipe(
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
    with("world",
        pipe(
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

  @Test(expected = UnsupportedOperationException.class)
  public void unsupportedActionType$simple() {
    new Action() {
      @Override
      public void accept(Visitor visitor) {
        visitor.visit(this);
      }
    }.accept(new ActionRunner.Impl());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void unsupportedActionType$composite() {
    new Composite.Base("unsupported", singletonList(nop())) {
      @Override
      public void accept(Visitor visitor) {
        visitor.visit(this);
      }
    }.accept(new ActionRunner.Impl());
  }
}
