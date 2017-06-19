package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.ActionFactory;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.reporting.ReportingActionPerformer;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertThat;

public class ForEachTest implements ActionFactory {
  @Test
  public void givenForEachAction$whenPerformWithReporting$worksCorrectly() {
    List<String> out = new LinkedList<>();
    // Given
    Action action = forEachOf(
        "Hello", "world", "!"
    ).perform(
        ($, s) -> $.sequential(asList(
            $.simple(
                "print {s}",
                () -> System.out.println("<" + s.get() + ">")
            ),
            $.simple(
                "add {s} to 'out'",
                () -> out.add("'" + s.get() + "'")
            )
        ))
    );
    // When
    TestUtils.createReportingActionPerformer(action).performAndReport();
    // Then
    assertThat(
        out,
        TestUtils.allOf(
            TestUtils.<List<String>, String>matcherBuilder()
                .transform("0thElement", list -> list.get(0))
                .check("=='Hello'", s -> Objects.equals(s, "'Hello'")),
            TestUtils.<List<String>, String>matcherBuilder()
                .transform("1stElement", list -> list.get(1))
                .check("=='world'", s -> Objects.equals(s, "'world'")),
            TestUtils.<List<String>, String>matcherBuilder()
                .transform("2ndElement", list -> list.get(2))
                .check("=='!'", s -> Objects.equals(s, "'!'"))
        ));
  }


  @Test
  public void givenConcurrentForEachAction$whenPerformWithReporting$worksCorrectly() throws InterruptedException {
    List<String> out = Collections.synchronizedList(new LinkedList<>());
    // Given
    Action action = forEachOf(
        "Hello", "world", "!"
    ).concurrently(
    ).perform(
        ($, s) -> $.sequential(
            $.simple(
                "print {s}",
                () -> System.out.println("<" + s.get() + ">")
            ),
            $.simple(
                "add {s} to 'out'",
                () -> out.add("'" + s.get() + "'")
            )
        )
    );
    // When3
    new ReportingActionPerformer.Builder(action).to(Writer.Std.ERR).build().performAndReport();
    // Then
    assertThat(
        out,
        TestUtils.allOf(
            TestUtils.<List<String>, List<String>>matcherBuilder()
                .transform("passthrough", list -> list)
                .check("contains:'Hello'", s -> s.contains("'Hello'")),
            TestUtils.<List<String>, List<String>>matcherBuilder()
                .transform("passthrough", list -> list)
                .check("contains:'world'", s -> s.contains("'world'")),
            TestUtils.<List<String>, List<String>>matcherBuilder()
                .transform("passthrough", list -> list)
                .check("contains:'!'", s -> s.contains("'!'"))
        ));
  }


  @Test(expected = IllegalStateException.class)
  public void givenConfusingForEachAction$whenPerformWithReporting$worksIllegalStateThrown() {
    // Given
    Action action = forEachOf(
        "Hello", "world", "!"
    ).sequentially(
    ).perform(
        ($, s) -> $.concurrent(asList(
            ActionFactory.Internal.nop(0, "YOU CANNOT CREATE ACTIONS OF THE SAME ID UNDER ONE forEachOf ACTION"),
            ActionFactory.Internal.nop(0, "YOU CANNOT CREATE ACTIONS OF THE SAME ID UNDER ONE forEachOf ACTION")
        ))
    );
    // When
    try {
      TestUtils.createReportingActionPerformer(action).performAndReport();
    } catch (IllegalStateException e) {
      // Then
      assertThat(
          e.getMessage(),
          TestUtils.allOf(
              CoreMatchers.containsString("More than one node matching"),
              CoreMatchers.containsString("Consider using 'named' action for them")
          )
      );
      throw e;
    }
  }

  @Test(expected = IllegalStateException.class)
  public void givenMismatchingForEachAction$whenPerformWithReporting$thenIllegalStateThrown() {
    // Given
    Action action = forEachOf(
        "Hello", "world", "!"
    ).sequentially(
    ).perform(
        ($, s) -> $.concurrent(asList(
            $.nop("Action 1"),
            $.nop("Action 2")
        ))
    );
    // When
    try {
      sequential(
          nop("confusing"),
          nop("action")
      ).accept(
          new ReportingActionPerformer.Builder(action).build()
      );
    } catch (IllegalStateException e) {
      // Then
      assertThat(
          e.getMessage(),
          TestUtils.allOf(
              CoreMatchers.containsString("Node matching '1-confusing' was not found under"),
              CoreMatchers.containsString("Node(ForEach (SEQUENTIALLY)")
          )
      );
      throw e;
    }
  }
}
