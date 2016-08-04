package com.github.dakusui.actionunit;

import com.github.dakusui.actionunit.visitors.ActionRunner;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.github.dakusui.actionunit.Actions.*;
import static com.google.common.base.Throwables.propagate;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

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

  @Test(timeout = 9000)
  public void concurrentTest() throws InterruptedException {
    final List<String> arr = new ArrayList<>();
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
    assertEquals("TimeOut (100[days])", timeout(nop(), 100, DAYS).describe());
  }

  @Test(expected = TimeoutException.class, timeout = 3000)
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

  @Test(timeout = 3000)
  public void repeatIncrementallyTest() {
    final List<String> arr = new ArrayList<>();
    repeatIncrementally(
        asList("1", "2"),
        forEach(new Block<String>() {
          @Override
          public void apply(String input) {
            arr.add(format("Hello %s", input));
          }
        })
    ).accept(new ActionRunner());
    assertArrayEquals(new Object[] { "Hello 1", "Hello 2" }, arr.toArray());
  }

  @Test
  public void nopTest() {
    // Just make sure no error happens
    Actions.nop().accept(new ActionRunner());
  }
}
