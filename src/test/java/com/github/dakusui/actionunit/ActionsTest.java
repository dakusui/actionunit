package com.github.dakusui.actionunit;

import com.github.dakusui.actionunit.TestOutput.Text;
import com.github.dakusui.actionunit.connectors.Connectors;
import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import com.google.common.base.Function;
import org.junit.ComparisonFailure;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.github.dakusui.actionunit.Action.ForEach.Mode.CONCURRENTLY;
import static com.github.dakusui.actionunit.Action.ForEach.Mode.SEQUENTIALLY;
import static com.github.dakusui.actionunit.Actions.*;
import static com.github.dakusui.actionunit.Utils.describe;
import static com.github.dakusui.actionunit.Utils.transform;
import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Iterables.toArray;
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
    Action.Composite action = (Action.Composite) sequential(nop(), nop(), nop());
    assertEquals(3, action.size());
  }

  @Test
  public void givenSequentialAction$whenDescribe$thenLooksNice() {
    assertEquals("Sequential (1 actions)", describe(sequential(nop())));
  }

  @Test(timeout = 9000)
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

  @Test(timeout = 9000)
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
      throw propagate(e);
    }
  }

  @Test(timeout = 9000, expected = NullPointerException.class)
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

  @Test(timeout = 9000, expected = Error.class)
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
        10,
        MILLISECONDS
    ).accept(new ActionRunner.Impl());
    assertArrayEquals(new Object[] { "Hello" }, arr.toArray());
  }

  @Test
  public void givenTimeoutAction$whenDescribe$thenLooksNice() {
    assertEquals("TimeOut (1[milliseconds])", describe(timeout(nop(), 1, MILLISECONDS)));
    assertEquals("TimeOut (10[seconds])", describe(timeout(nop(), 10000, MILLISECONDS)));
    assertEquals("TimeOut (1000[days])", describe(timeout(nop(), 1000, DAYS)));
  }

  @Test(expected = TimeoutException.class)
  public void timeoutTest$timeout() throws Throwable {
    final List<String> arr = new ArrayList<>();
    try {
      timeout(simple(new Runnable() {
            @Override
            public void run() {
              arr.add("Hello");
              try {
                TimeUnit.SECONDS.sleep(1);
              } catch (InterruptedException e) {
                throw propagate(e);
              }
            }
          }),
          1,
          MILLISECONDS
      ).accept(new ActionRunner.Impl());
    } catch (ActionException e) {
      throw e.getCause();
    } finally {
      assertArrayEquals(new Object[] { "Hello" }, arr.toArray());
    }
  }

  @Test(expected = RuntimeException.class, timeout = 3000)
  public void timeoutTest$throwsRuntimeException() throws Throwable {
    final List<String> arr = new ArrayList<>();
    try {
      timeout(simple(new Runnable() {
            @Override
            public void run() {
              arr.add("Hello");
              throw new RuntimeException();
            }
          }),
          1,
          MILLISECONDS
      ).accept(new ActionRunner.Impl());
    } catch (ActionException e) {
      throw e.getCause();
    } finally {
      assertArrayEquals(new Object[] { "Hello" }, arr.toArray());
    }
  }

  @Test(expected = Error.class, timeout = 3000)
  public void timeoutTest$throwsError() throws Throwable {
    final List<String> arr = new ArrayList<>();
    try {
      timeout(simple(new Runnable() {
            @Override
            public void run() {
              arr.add("Hello");
              throw new Error();
            }
          }),
          1,
          MILLISECONDS
      ).accept(new ActionRunner.Impl());
    } catch (ActionException e) {
      throw e.getCause();
    } finally {
      assertArrayEquals(new Object[] { "Hello" }, arr.toArray());
    }
  }

  @Test(timeout = 3000)
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

  @Test(timeout = 3000)
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
  public void givenRetryAction$whenGiveUpException$thenAborted() {
    final List<String> arr = new ArrayList<>();
    try {
      retry(simple(new Runnable() {
            @Override
            public void run() {
              throw Abort.abort();
            }
          }),
          1, 1, MILLISECONDS
      ).accept(new ActionRunner.Impl());
    } finally {
      assertEquals(Collections.emptyList(), arr);
    }
  }

  @Test(expected = IOException.class)
  public void givenRetryAction$whenGiveUpException2$thenAborted() throws Throwable {
    final List<String> arr = new ArrayList<>();
    try {
      retry(simple(new Runnable() {
            @Override
            public void run() {
              throw Abort.abort(new IOException());
            }
          }),
          1, 1, MILLISECONDS
      ).accept(new ActionRunner.Impl());
    } catch (Abort e) {
      throw e.getCause();
    } finally {
      assertEquals(Collections.emptyList(), arr);
    }
  }

  @Test(expected = ActionException.class, timeout = 3000)
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

  @Test(timeout = 3000)
  public void forEachTest() {
    final List<String> arr = new ArrayList<>();
    forEach(
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
        describe(forEach(
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
  public void givenForEachActionViaNonCollection$whenDescribe$thenLooksNice() {
    assertEquals(
        "ForEach (Sequential, ? items) {empty!}",
        describe(forEach(
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
    forEach(
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


  @Test(timeout = 2000)
  public void givenWaitForAction$whenPerform$thenExpectedAmountOfTimeSpent() {
    ////
    // To force JVM load classes used by this test, run the action once for warm-up.
    waitFor(1, TimeUnit.MILLISECONDS).accept(new ActionRunner.Impl());
    ////
    // Let's do the test.
    long before = currentTimeMillis();
    waitFor(1, TimeUnit.MILLISECONDS).accept(new ActionRunner.Impl());
    //noinspection unchecked
    assertThat(
        currentTimeMillis() - before,
        allOf(
            greaterThanOrEqualTo(1L),
            ////
            // Depending on unpredictable conditions, such as JVM's internal state,
            // GC, class loading, etc.,  "waitFor" action may take a longer time
            // than 1 msec to perform. In this case I'm giving 3 msec including
            // grace period.
            lessThan(3L)
        )
    );
  }

  @Test
  public void givenPipeAction$whenPerformed$thenWorksFine() {
    with("world",
        pipe(
            Connectors.<String>context(),
            new Function<String, Text>() {
              @Override
              public Text apply(String s) {
                return new Text("hello:" + s);
              }
            }, new Sink<Text>() {
              @Override
              public void apply(Text input, Context context) {
                assertEquals("hello:world", input.value());
              }
            })).accept(new ActionRunner.Impl());
  }

  @Test
  public void givenSimplePipeAction$whenPerformed$thenWorksFine() {
    final List<Text> out = new LinkedList<>();
    forEach(asList("world", "WORLD"),
        pipe(
            new Function<String, Text>() {
              @Override
              public Text apply(String s) {
                return new Text("hello:" + s);
              }
            },
            new Sink<Text>() {
              @Override
              public void apply(Text input, Context context) {
                out.add(input);
              }
            }
        )).accept(new ActionRunner.Impl());
    assertArrayEquals(
        asList("hello:world", "hello:WORLD").toArray(new String[2]),
        toArray(transform(out, new Function<Text, String>() {
          @Override
          public String apply(Text input) {
            return input.value();
          }
        }), String.class)
    );
  }

  @Test
  public void givenSimplestPipeAction$whenPerformed$thenWorksFine() {
    final List<Text> out = new LinkedList<>();
    with("world",
        pipe(
            new Function<String, Text>() {
              @Override
              public Text apply(String s) {
                out.add(new Text("hello:" + s));
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
            new Function<String, Text>() {
              @Override
              public Text apply(String s) {
                return new Text("Hello:" + s);
              }
            }, new Sink<Text>() {
              @Override
              public void apply(Text input, Context context) {
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
            new Function<String, Text>() {
              @Override
              public Text apply(String s) {
                return new Text("Hello:" + s);
              }
            },
            new Sink<Text>() {
              @Override
              public void apply(Text input, Context context) {
                out.add(input.value());
              }
            },
            new Sink<Text>() {
              @Override
              public void apply(Text input, Context context) {
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
    new Action.Composite.Base("unsupported", singletonList(nop())) {
      @Override
      public void accept(Visitor visitor) {
        visitor.visit(this);
      }
    }.accept(new ActionRunner.Impl());
  }
}
