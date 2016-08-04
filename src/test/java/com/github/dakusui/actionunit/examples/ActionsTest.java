package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.github.dakusui.actionunit.Actions.*;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
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

  @Test
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
        SECONDS
    ).accept(new ActionRunner());
    assertArrayEquals(new Object[] { "Hello" }, arr.toArray());
  }

  @Test
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

  @Test
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
                throw new Action.Exception("fail");
              }
            }
          }),
          1, 1, MILLISECONDS
      ).accept(new ActionRunner());
    } finally {
      assertArrayEquals(new Object[] { "Hello", "Hello" }, arr.toArray());
    }
  }

  @Test(expected = Action.Exception.class)
  public void retryTest$failForever() {
    final List<String> arr = new ArrayList<>();
    try {
      retry(simple(new Runnable() {
            @Override
            public void run() {
              arr.add("Hello");
              throw new Action.Exception("fail");
            }
          }),
          1, 1, MILLISECONDS
      ).accept(new ActionRunner());
    } finally {
      assertArrayEquals(new Object[] { "Hello", "Hello" }, arr.toArray());
    }
  }

  @Test
  public void repeatIncrementallyTest() {
    final List<String> arr = new ArrayList<>();
    repeatIncrementally(
        new Action.WithTarget.Factory<String>() {
          @Override
          public Action create(final String target) {
            return simple(this.describe(), new Runnable() {
              @Override
              public void run() {
                arr.add(format("Hello %s", target));
              }
            });
          }

          @Override
          public String describe() {
            return "(noname)";
          }
        },
        asList("1", "2")
    ).accept(new ActionRunner());
    assertArrayEquals(new Object[] { "Hello 1", "Hello 2" }, arr.toArray());
  }
}
