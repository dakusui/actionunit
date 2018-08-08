package com.github.dakusui.actionunit.scenarios;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.ContextConsumer;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.github.dakusui.actionunit.core.ActionSupport.*;
import static com.github.dakusui.actionunit.exceptions.ActionException.wrap;
import static com.github.dakusui.actionunit.ut.utils.TestUtils.createActionPerformer;
import static com.github.dakusui.crest.Crest.asLong;
import static com.github.dakusui.crest.Crest.assertThat;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static java.util.Collections.synchronizedList;
import static java.util.concurrent.TimeUnit.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class CompatActionSupportTest {
  @Test
  public void simpleTest() {
    final List<String> arr = new ArrayList<>();
    simple("Add 'Hello'", (c) -> arr.add("Hello")).accept(createActionPerformer());
    assertArrayEquals(arr.toArray(), new Object[] { "Hello" });
  }

  @Test
  public void sequentialTest() {
    final List<String> arr = new ArrayList<>();
    sequential(
        simple("Add 'Hello A", c -> arr.add("Hello A")),
        simple("Add 'Hello B'", c -> arr.add("Hello B"))
    ).accept(createActionPerformer());
    assertEquals(asList("Hello A", "Hello B"), arr);
  }

  @Test(timeout = 3000000)
  public void concurrentTest() {
    final List<String> arr = synchronizedList(new ArrayList<>());
    parallel(
        simple("Add 'Hello A'", (c) -> arr.add("Hello A")),
        simple("Add 'Hello B'", (c) -> arr.add("Hello B"))
    ).accept(createActionPerformer());
    Collections.sort(arr);
    assertEquals(asList("Hello A", "Hello B"), arr);
  }

  @SuppressWarnings({ "unchecked", "SuspiciousToArrayCall" })
  @Test(timeout = 3000000)
  public void concurrentTest$checkConcurrency() {
    final List<Map.Entry<Long, Long>> arr = synchronizedList(new ArrayList<>());
    parallel(
        simple(
            "create entry (1)",
            (c) -> arr.add(createEntry())),
        simple(
            "create entry (2)",
            (c) -> arr.add(createEntry()))
    ).accept(createActionPerformer());
    for (Map.Entry<Long, Long> i : arr) {
      for (Map.Entry<Long, Long> j : arr) {
        assertThat(
            i,
            asLong("getValue").gt(j.getKey()).$()
        );
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
  public void concurrentTest$runtimeExceptionThrown() {
    // given
    final List<String> arr = synchronizedList(new ArrayList<>());
    // when
    parallel(
        simple("Add 'Hello A' and throw NPE",
            (c) -> {
              arr.add("Hello A");
              throw new NullPointerException();
            }),
        simple("Add 'hello B'", (c) -> arr.add("Hello B"))
    ).accept(createActionPerformer());
  }

  @Test(timeout = 3000000, expected = Error.class)
  public void concurrentTest$errorThrown() {
    // given
    final List<String> arr = synchronizedList(new ArrayList<>());
    // when
    parallel(
        simple("Add 'Hello A'", (c) -> {
          arr.add("Hello A");
          throw new Error();
        }),
        simple("Add 'Hello B'", (c) -> arr.add("Hello B"))
    ).accept(createActionPerformer());
  }

  @Test
  public void timeoutTest() {
    final List<String> arr = new ArrayList<>();
    timeout(simple("Add 'Hello'", (c) -> arr.add("Hello"))).in(
        ////
        // 10 msec should be sufficient to finish the action above.
        10, MILLISECONDS
    ).accept(createActionPerformer());
    assertArrayEquals(new Object[] { "Hello" }, arr.toArray());
  }

  @Test
  public void givenTimeoutAction$whenDescribe$thenLooksNice() {
    assertEquals("timeout in 1[milliseconds]", String.format("%s", timeout(nop()).in(1, MILLISECONDS)));
    assertEquals("timeout in 10[seconds]", String.format("%s", timeout(nop()).in(10000, MILLISECONDS)));
    assertEquals("timeout in 1000[days]", String.format("%s", (timeout(nop()).in(1000, DAYS))));
  }

  @Test(expected = TimeoutException.class)
  public void timeoutTest$timeout() throws Throwable {
    final List<String> arr = new ArrayList<>();
    try {
      ReportingActionPerformer.create(Writer.Std.OUT).performAndReport(
          timeout(
              simple("Add 'Hello' and sleep 30[msec]", (c) -> {
                arr.add("Hello");
                try {
                  TimeUnit.SECONDS.sleep(30);
                } catch (InterruptedException e) {
                  throw wrap(e);
                }
              })
          ).in(
              1, MILLISECONDS
          ));
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
              (c) -> {
                arr.add("Hello");
                throw new RuntimeException();
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
          simple("Add 'Hello' and throw Error", (c) -> {
            arr.add("Hello");
            throw new Error();
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
        simple("Add 'Hello'", (c) -> arr.add("Hello"))
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
            new ContextConsumer() {
              int i = 0;

              @Override
              public void accept(Context context) {
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

  @Test(expected = ActionException.class, timeout = 300000)
  public void retryTest$failForever() {
    final List<String> arr = new ArrayList<>();
    retry(
        simple("", (c) -> {
          arr.add("Hello");
          throw new ActionException("fail");
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
    assertEquals("retry once in 2[seconds] on Exception", String.format("%s", retry(nop()).times(1).withIntervalOf(2, SECONDS).build()));
  }

  @Test
  public void nopTest() {
    // Just make sure no error happens
    nop().accept(createActionPerformer());
  }

  @Test
  public void givenWhenAction$whenPerform$thenObjectAdded() {
    List<Object> objects = new LinkedList<>();

    Action action = when(
        (c) -> true
    ).perform(
        simple("meets", (c) -> objects.add(new Object()))
    ).otherwise(
        nop()
    );
    action.accept(createActionPerformer());

    assertEquals(1, objects.size());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void unsupportedActionType$simple() {
    new Action() {
      @Override
      public void formatTo(Formatter formatter, int flags, int width, int precision) {
        formatter.format(this.toString());
      }

      @Override
      public void accept(Visitor visitor) {
        visitor.visit(this);
      }
    }.accept(createActionPerformer());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void unsupportedActionType$composite() {
    new Action() {
      @Override
      public void formatTo(Formatter formatter, int flags, int width, int precision) {
        formatter.format("%s", "dummy");
      }

      @Override
      public void accept(Action.Visitor visitor) {
        visitor.visit(this);
      }
    }.accept(createActionPerformer());
  }
}
