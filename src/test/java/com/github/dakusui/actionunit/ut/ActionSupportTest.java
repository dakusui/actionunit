package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.actions.Composite;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.ActionSupport;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.utils.Abort;
import com.github.dakusui.actionunit.utils.TestUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.github.dakusui.actionunit.core.ActionSupport.*;
import static com.github.dakusui.actionunit.exceptions.ActionException.wrap;
import static com.github.dakusui.actionunit.helpers.InternalUtils.describe;
import static com.github.dakusui.actionunit.utils.TestUtils.createActionPerformer;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.synchronizedList;
import static java.util.concurrent.TimeUnit.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ActionSupportTest {
  @Test
  public void simpleTest() {
    final List<String> arr = new ArrayList<>();
    ActionSupport.simple("Add 'Hello'", () -> arr.add("Hello")).accept(createActionPerformer());
    assertArrayEquals(arr.toArray(), new Object[] { "Hello" });
  }

  @Test
  public void sequentialTest() {
    final List<String> arr = new ArrayList<>();
    sequential(
        ActionSupport.simple("Add 'Hello A", new Runnable() {
          @Override
          public void run() {
            arr.add("Hello A");
          }
        }),
        simple("Add 'Hello B'", new Runnable() {
          @Override
          public void run() {
            arr.add("Hello B");
          }
        })
    ).accept(createActionPerformer());
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

  @Test(timeout = 3000000)
  public void concurrentTest() throws InterruptedException {
    final List<String> arr = synchronizedList(new ArrayList<String>());
    concurrent(
        simple("Add 'Hello A'", new Runnable() {
          @Override
          public void run() {
            arr.add("Hello A");
          }
        }),
        simple("Add 'Hello B'", new Runnable() {
          @Override
          public void run() {
            arr.add("Hello B");
          }
        })
    ).accept(createActionPerformer());
    Collections.sort(arr);
    assertEquals(asList("Hello A", "Hello B"), arr);
  }

  @Test(timeout = 3000000)
  public void concurrentTest$checkConcurrency() throws InterruptedException {
    final List<Map.Entry<Long, Long>> arr = synchronizedList(new ArrayList<Map.Entry<Long, Long>>());
    concurrent(
        ActionSupport.simple(
            "create entry (1)",
            () -> arr.add(createEntry())),
        ActionSupport.simple(
            "create entry (2)",
            () -> arr.add(createEntry()))
    ).accept(createActionPerformer());
    for (Map.Entry<Long, Long> i : arr) {
      for (Map.Entry<Long, Long> j : arr) {
        assertThat(i.getValue(), greaterThan(j.getKey()));
      }
    }
  }

  private Map.Entry<Long, Long> createEntry() {
    long before = currentTimeMillis();
    try {
      TimeUnit.MILLISECONDS.sleep(1000);
      return new AbstractMap.SimpleEntry<>(
          before,
          currentTimeMillis()
      );
    } catch (InterruptedException e) {
      throw wrap(e);
    }
  }

  @Test(timeout = 3000000, expected = NullPointerException.class)
  public void concurrentTest$runtimeExceptionThrown() throws InterruptedException {
    // given
    final List<String> arr = synchronizedList(new ArrayList<String>());
    // when
    concurrent(
        simple("Add 'Hello A' and throw NPE",
            () -> {
              arr.add("Hello A");
              throw new NullPointerException();
            }),
        simple("Add 'hello B'", () -> arr.add("Hello B"))
    ).accept(createActionPerformer());
  }

  @Test(timeout = 3000000, expected = Error.class)
  public void concurrentTest$errorThrown() throws InterruptedException {
    // given
    final List<String> arr = synchronizedList(new ArrayList<String>());
    // when
    concurrent(
        simple("Add 'Hello A'", () -> {
          arr.add("Hello A");
          throw new Error();
        }),
        simple("Add 'Hello B'", () -> arr.add("Hello B"))
    ).accept(createActionPerformer());
  }

  @Test
  public void timeoutTest() {
    final List<String> arr = new ArrayList<>();
    timeout(simple("Add 'Hello'", new Runnable() {
      @Override
      public void run() {
        arr.add("Hello");
      }
    })).in(
        ////
        // 10 msec should be sufficient to finish the action above.
        10, MILLISECONDS
    ).accept(createActionPerformer());
    assertArrayEquals(new Object[] { "Hello" }, arr.toArray());
  }

  @Test
  public void givenTimeoutAction$whenDescribe$thenLooksNice() {
    assertEquals("TimeOut(1[milliseconds])", describe(timeout(nop()).in(1, MILLISECONDS)));
    assertEquals("TimeOut(10[seconds])", describe(timeout(nop()).in(10000, MILLISECONDS)));
    assertEquals("TimeOut(1000[days])", describe(timeout(nop()).in(1000, DAYS)));
  }

  @Test(expected = TimeoutException.class)
  public void timeoutTest$timeout() throws Throwable {
    final List<String> arr = new ArrayList<>();
    try {
      timeout(
          simple("Add 'Hello' and sleep 30[msec]", new Runnable() {
            @Override
            public void run() {
              arr.add("Hello");
              try {
                TimeUnit.SECONDS.sleep(30);
              } catch (InterruptedException e) {
                throw wrap(e);
              }
            }
          })
      ).in(
          1, MILLISECONDS
      ).accept(createActionPerformer());
    } catch (ActionException e) {
      assertArrayEquals(new Object[] { "Hello" }, arr.toArray());
      throw e.getCause();
    }
  }

  @Test(expected = RuntimeException.class, timeout = 300000)
  public void givenTimeOutAtTopLevel$whenRuntimeExceptionThrownFromInside$thenRuntimeException() throws Throwable {
    final List<String> arr = new ArrayList<>();
    try {
      timeout(
          simple(
              "Add 'Hello' and throw RuntimeException",
              new Runnable() {
                @Override
                public void run() {
                  arr.add("Hello");
                  throw new RuntimeException();
                }
              })
      ).in(
          100, MILLISECONDS
      ).accept(createActionPerformer());
    } catch (ActionException e) {
      assertArrayEquals(new Object[] { "Hello" }, arr.toArray());
      throw e.getCause();
    }
  }

  @Test(expected = Error.class, timeout = 300000)
  public void givenTimeOutAtTopLevel$whenErrorThrownFromInside$thenError() throws Throwable {
    final List<String> arr = new ArrayList<>();
    try {
      timeout(
          simple("Add 'Hello' and throw Error", new Runnable() {
            @Override
            public void run() {
              arr.add("Hello");
              throw new Error();
            }
          })
      ).in(
          100, MILLISECONDS
      ).accept(createActionPerformer());
    } catch (ActionException e) {
      assertArrayEquals(new Object[] { "Hello" }, arr.toArray());
      throw e.getCause();
    }
  }

  @Test(timeout = 300000)
  public void retryTest() {
    final List<String> arr = new ArrayList<>();
    retry(
        simple("Add 'Hello'", new Runnable() {
          @Override
          public void run() {
            arr.add("Hello");
          }
        })
    ).times(
        0
    ).withIntervalOf(
        1, MILLISECONDS
    ).build(
    ).accept(createActionPerformer());
    assertArrayEquals(new Object[] { "Hello" }, arr.toArray());
  }

  @Test(timeout = 300000)
  public void retryTest$failOnce() {
    final List<String> arr = new ArrayList<>();
    retry(
        simple(
            "Add 'Hello' and fail on first try.",
            new Runnable() {
              int i = 0;

              @Override
              public void run() {
                arr.add("Hello");
                if (i < 1) {
                  i++;
                  throw new ActionException("fail");
                }
              }
            })
    ).times(
        1
    ).withIntervalOf(
        1, MILLISECONDS
    ).build(
    ).accept(createActionPerformer());
    assertArrayEquals(new Object[] { "Hello", "Hello" }, arr.toArray());
  }

  @Test(expected = Abort.class)
  public void givenRetryAction$whenAbortException$thenAborted() {
    final TestUtils.Out out = new TestUtils.Out();
    retry(
        simple("Write 'run' and Abort.abort", () -> {
          out.writeLine("run");
          throw Abort.abort();
        })
    ).times(
        2
    ).withIntervalOf(
        1, MILLISECONDS
    ).build(
    ).accept(createActionPerformer());
    assertThat(out, hasSize(1));
  }

  @Test(expected = IOException.class)
  public void givenRetryAction$whenAbortException2$thenAbortedAndRootExceptionStoredProperly() throws Throwable {
    final TestUtils.Out out = new TestUtils.Out();
    try {
      retry(
          simple(
              "Write 'Hello' and Abort.abort env IOException",
              new Runnable() {
                @Override
                public void run() {
                  out.writeLine("Hello");
                  throw Abort.abort(new IOException());
                }
              })
      ).times(
          2
      ).withIntervalOf(
          1, MILLISECONDS
      ).build(
      ).accept(createActionPerformer());
    } catch (Abort e) {
      assertThat(out, hasSize(1));
      throw e.getCause();
    }
  }

  @Test(expected = ActionException.class, timeout = 300000)
  public void retryTest$failForever() {
    final List<String> arr = new ArrayList<>();
    retry(
        simple("", new Runnable() {
          @Override
          public void run() {
            arr.add("Hello");
            throw new ActionException("fail");
          }
        })
    ).times(
        1
    ).withIntervalOf(
        1, MILLISECONDS
    ).build(
    ).accept(createActionPerformer());
    assertArrayEquals(new Object[] { "Hello", "Hello" }, arr.toArray());
  }

  @Test
  public void givenRetryAction$whenDescribe$thenLooksNice() {
    assertEquals("Retry(2[seconds]x1times)", describe(retry(nop()).times(1).withIntervalOf(2, SECONDS).build()));
  }

  @Test(timeout = 3000000)
  public void givenNothingForChildAction$whenWhilActionPerformedWithAlwaysFalseCondition$thenQuitImmediately() {
    Action action = whilst(
        () -> "Hello", t -> false
    ).perform(
        ($) -> nop()
    );
    action.accept(createActionPerformer());
  }

  @SafeVarargs
  public static <T> List<T> autocloseableList(final TestUtils.Out out, final String msg, final T... values) {
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
  public void nopTest() {
    // Just make sure no error happens
    ActionSupport.nop().accept(createActionPerformer());
  }


  @Test(timeout = 3000000)
  public void givenSleepAction$whenPerform$thenExpectedAmountOfTimeSpent() {
    ////
    // To force JVM load classes used by this test, run the action once for warm-up.
    sleep(1, TimeUnit.MILLISECONDS).accept(createActionPerformer());
    ////
    // Let's do the test.
    long before = currentTimeMillis();
    sleep(1, TimeUnit.MILLISECONDS).accept(createActionPerformer());
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
            lessThan(5L)
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
  public void givenWhenAction$whenPerform$thenObjectAdded() {
    List<Object> objects = new LinkedList<>();

    Action action = when(
        () -> "Hello".startsWith("H")
    ).perform(
        simple("meets", () -> objects.add(new Object()))
    ).otherwise(
        nop()
    );
    action.accept(createActionPerformer());

    assertEquals(1, objects.size());
  }

  @Test
  public void givenWhileAction$whenPerform$then100ObjectAdded() {
    List<Object> objects = new LinkedList<>();

    Action action = whilst(
        () -> objects.size() < 100
    ).perform(
        simple("meets", () -> objects.add(new Object()))
    );
    action.accept(createActionPerformer());

    assertEquals(100, objects.size());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void unsupportedActionType$simple() {
    new Action() {
      @Override
      public void accept(Visitor visitor) {
        visitor.visit(this);
      }

      @Override
      public int id() {
        return 0;
      }
    }.accept(createActionPerformer());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void unsupportedActionType$composite() {
    new Composite.Base(0, "unsupported", singletonList(nop())) {
      @Override
      public void accept(Visitor visitor) {
        visitor.visit(this);
      }
    }.accept(createActionPerformer());
  }
}
