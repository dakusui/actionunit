package com.github.dakusui.actionunit;

import com.github.dakusui.actionunit.visitors.ActionRunner;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.github.dakusui.actionunit.Action.ForEach.Mode.CONCURRENTLY;
import static com.github.dakusui.actionunit.Action.ForEach.Mode.SEQUENTIALLY;
import static com.github.dakusui.actionunit.Actions.*;
import static com.google.common.base.Throwables.propagate;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.synchronizedList;
import static java.util.concurrent.TimeUnit.*;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.core.AllOf.allOf;
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
    }).accept(new ActionRunner());
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
    ).accept(new ActionRunner());
    assertEquals(asList("Hello A", "Hello B"), arr);
  }

  @Test
  public void givenSequentialAction$whenDescribe$thenLooksNice() {
    assertEquals("(noname) (Sequential, 1 actions)", sequential(nop()).describe());
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
    ).accept(new ActionRunner());
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
      ).accept(new ActionRunner());
    } finally {
      for (Map.Entry<Long, Long> i : arr) {
        for (Map.Entry<Long, Long> j : arr) {
          assertTrue(i.getValue() > j.getKey());
        }
      }
    }
  }

  private Map.Entry<Long, Long> createEntry() {
    long before = System.currentTimeMillis();
    try {
      TimeUnit.MILLISECONDS.sleep(100);
      return new AbstractMap.SimpleEntry<>(
          before,
          System.currentTimeMillis()
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
      ).accept(new ActionRunner());
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
      ).accept(new ActionRunner());
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
        1,
        MILLISECONDS
    ).accept(new ActionRunner());
    assertArrayEquals(new Object[] { "Hello" }, arr.toArray());
  }

  @Test
  public void givenTimeoutAction$whenDescribe$thenLooksNice() {
    assertEquals("TimeOut (1[milliseconds])", timeout(nop(), 1, MILLISECONDS).describe());
    assertEquals("TimeOut (10[seconds])", timeout(nop(), 10000, MILLISECONDS).describe());
    assertEquals("TimeOut (1000[days])", timeout(nop(), 1000, DAYS).describe());
  }

  @Test(expected = TimeoutException.class, timeout = 10000)
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
      ).accept(new ActionRunner());
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
      ).accept(new ActionRunner());
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
      ).accept(new ActionRunner());
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
    ).accept(new ActionRunner());
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
      ).accept(new ActionRunner());
    } finally {
      assertArrayEquals(new Object[] { "Hello", "Hello" }, arr.toArray());
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
      ).accept(new ActionRunner());
    } finally {
      assertArrayEquals(new Object[] { "Hello", "Hello" }, arr.toArray());
    }
  }

  @Test
  public void givenRetryAction$whenDescribe$thenLooksNice() {
    assertEquals("Retry(2[seconds]x1times)", retry(nop(), 1, 2, SECONDS).describe());
  }

  @Test//(timeout = 3000)
  public void forEachTest() {
    final List<String> arr = new ArrayList<>();
    forEach(
        asList("1", "2"),
        new Block.Base<String>("print") {
          @Override
          public void apply(String input) {
            arr.add(String.format("Hello %s", input));
          }
        }
    ).accept(new ActionRunner());
    System.out.println(arr);
    assertArrayEquals(new Object[] { "Hello 1", "Hello 2" }, arr.toArray());
  }

  @Test
  public void givenForEachAction$whenDescribe$thenLooksNice() {
    assertEquals(
        "ForEach (Concurrent, 2 items) { (noname) }",
        forEach(
            asList("hello", "world"),
            CONCURRENTLY,
            new Block.Base<String>() {
              @Override
              public void apply(String s) {
              }
            }
        ).describe()
    );
  }

  @Test
  public void givenForEachActionViaNonCollection$whenDescribe$thenLooksNice() {
    assertEquals(
        "ForEach (Sequential, ? items) { empty! }",
        forEach(
            new Iterable<String>() {
              @Override
              public Iterator<String> iterator() {
                return asList("hello", "world").iterator();
              }
            },
            SEQUENTIALLY,
            new Block.Base<String>("empty!") {
              @Override
              public void apply(String s) {
              }
            }
        ).describe()
    );
  }

  @Test
  public void nopTest() {
    // Just make sure no error happens
    Actions.nop().accept(new ActionRunner());
  }


  @Test(timeout = 2000)
  public void givenWaitForAction$whenPerform$thenExpectedAmountOfTimeSpent() {
    ////
    // To force JVM load classes used by this test, run the action once for warm-up.
    waitFor(1, TimeUnit.MILLISECONDS).accept(new ActionRunner());
    ////
    // Let's do the test.
    long before = System.currentTimeMillis();
    waitFor(1, TimeUnit.MILLISECONDS).accept(new ActionRunner());
    assertThat(
        System.currentTimeMillis() - before,
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

  @Test(expected = UnsupportedOperationException.class)
  public void unsupportedActionType$simple() {
    new Action() {
      @Override
      public void accept(Visitor visitor) {
        visitor.visit(this);
      }

      @Override
      public String describe() {
        return "unsupported";
      }
    }.accept(new ActionRunner());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void unsupportedActionType$composite() {
    new Action.Composite("unsupported", singletonList(nop())) {
      @Override
      public void accept(Visitor visitor) {
        visitor.visit(this);
      }
    }.accept(new ActionRunner());
  }
}
