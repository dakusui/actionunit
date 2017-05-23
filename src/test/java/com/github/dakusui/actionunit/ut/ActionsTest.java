package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.Actions;
import com.github.dakusui.actionunit.actions.Composite;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.utils.Abort;
import com.github.dakusui.actionunit.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.github.dakusui.actionunit.Actions.*;
import static com.github.dakusui.actionunit.Utils.describe;
import static com.github.dakusui.actionunit.exceptions.ActionException.wrap;
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

  @Test(timeout = 3000000)
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

  @Test(timeout = 3000000)
  public void concurrentTest$checkConcurrency() throws InterruptedException {
    final List<Map.Entry<Long, Long>> arr = synchronizedList(new ArrayList<Map.Entry<Long, Long>>());
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
  }

  @Test(timeout = 3000000, expected = Error.class)
  public void concurrentTest$errorThrown() throws InterruptedException {
    // given
    final List<String> arr = synchronizedList(new ArrayList<String>());
    // when
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
      assertArrayEquals(new Object[] { "Hello" }, arr.toArray());
      throw e.getCause();
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
      assertArrayEquals(new Object[] { "Hello" }, arr.toArray());
      throw e.getCause();
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
      assertArrayEquals(new Object[] { "Hello" }, arr.toArray());
      throw e.getCause();
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
    assertArrayEquals(new Object[] { "Hello", "Hello" }, arr.toArray());
  }

  @Test(expected = Abort.class)
  public void givenRetryAction$whenAbortException$thenAborted() {
    final TestUtils.Out out = new TestUtils.Out();
    retry(simple(new Runnable() {
          @Override
          public void run() {
            out.writeLine("run");
            throw Abort.abort();
          }
        }),
        2, 1, MILLISECONDS
    ).accept(new ActionRunner.Impl());
    assertThat(out, hasSize(1));
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
      assertThat(out, hasSize(1));
      throw e.getCause();
    }
  }

  @Test(expected = ActionException.class, timeout = 300000)
  public void retryTest$failForever() {
    final List<String> arr = new ArrayList<>();
    retry(simple(new Runnable() {
          @Override
          public void run() {
            arr.add("Hello");
            throw new ActionException("fail");
          }
        }),
        1, 1, MILLISECONDS
    ).accept(new ActionRunner.Impl());
    assertArrayEquals(new Object[] { "Hello", "Hello" }, arr.toArray());
  }

  @Test
  public void givenRetryAction$whenDescribe$thenLooksNice() {
    assertEquals("Retry(2[seconds]x1times)", describe(retry(nop(), 1, 2, SECONDS)));
  }

  @Test(timeout = 3000000)
  public void givenNothingForChildAction$whenWhilActionPerformedWithAlwaysFalseCondition$thenQuitImmediately() {
    Action action = repeatwhile(v -> false);
    action.accept(new ActionRunner.Impl());
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
  public void givenTestAction$whenBuiltAndPerformed$thenWorksFine() {

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
