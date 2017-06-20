package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.actions.ForEach;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.utils.Matchers;
import com.github.dakusui.actionunit.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.reporting.ReportingActionPerformer;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static com.github.dakusui.actionunit.utils.Matchers.allOf;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

public class ForEachTest implements Context {
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
        allOf(
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
        allOf(
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


  @Test
  public void givenConfusingForEachAction$whenPerformWithReporting$worksIllegalStateThrown() {
    // Given
    Action action = createConfusingAction();
    // When
    try {
      TestUtils.createReportingActionPerformer(action).performAndReport();
      // The statement above must throw an exception
      Assert.fail();
    } catch (IllegalStateException e) {
      // Then
      assertThat(
          e.getMessage(),
          allOf(
              containsString("More than one node whose id is "),
              containsString("Examine they are created in an appropriate context.")
          )
      );
    }
  }

  @Test
  public void givenConfusingForEachAction$whenPerformWithReportingAndIdentificationDoneByName$worksIllegalStateThrown() {
    // Given
    Action action = createConfusingAction();
    // When
    try {
      new ReportingActionPerformer.Builder(action).with(
          ReportingActionPerformer.Identifier.BY_NAME
      ).build(
      ).performAndReport();
      // The statement above must throw an exception
      Assert.fail();
    } catch (IllegalStateException e) {
      // Then
      assertThat(
          e.getMessage(),
          allOf(
              containsString("More than one node matching"),
              containsString("Consider using 'named' action for them.")
          )
      );
    }
  }

  public ForEach<String> createConfusingAction() {
    return forEachOf(
        "Hello", "world", "!"
    ).sequentially(
    ).perform(
        ($, s) -> $.concurrent(asList(
            Internal.nop(0, "YOU CANNOT CREATE ACTIONS OF THE SAME ID UNDER ONE forEachOf ACTION"),
            Internal.nop(0, "YOU CANNOT CREATE ACTIONS OF THE SAME ID UNDER ONE forEachOf ACTION")
        ))
    );
  }

  @Test
  public void givenMismatchingForEachAction$whenPerformWithReporting$thenIllegalStateThrown() {
    // Given
    Action action = createBrokenAction();
    // When
    try {
      sequential(
          nop("confusing"),
          nop("action")
      ).accept(
          new ReportingActionPerformer.Builder(action).build()
      );
      // The statement above must throw an exception
      Assert.fail();
    } catch (IllegalStateException e) {
      // Then
      assertThat(
          e.getMessage(),
          allOf(
              containsString("Node matching '1(confusing)' was not found under"),
              containsString("0(ForEach (SEQUENTIALLY)")
          )
      );
    }
  }

  @Test
  public void givenMismatchingForEachAction$whenPerformWithReportingAndIdetificationDoneByName$thenIllegalStateThrown() {
    // Given
    Action action = createBrokenAction();
    // When
    try {
      sequential(
          nop("confusing"),
          nop("action")
      ).accept(
          new ReportingActionPerformer.Builder(action).with(ReportingActionPerformer.Identifier.BY_NAME).build()
      );
      // The statement above must throw an exception
      Assert.fail();
    } catch (IllegalStateException e) {
      // Then
      assertThat(
          e.getMessage(),
          allOf(
              containsString("Node matching 'confusing' was not found under"),
              containsString("ForEach (SEQUENTIALLY)")
          )
      );
    }
  }

  public ForEach<String> createBrokenAction() {
    return forEachOf(
        "Hello", "world", "!"
    ).sequentially(
    ).perform(
        ($, s) -> $.concurrent(asList(
            $.nop("Action 1"),
            $.nop("Action 2")
        ))
    );
  }
}
