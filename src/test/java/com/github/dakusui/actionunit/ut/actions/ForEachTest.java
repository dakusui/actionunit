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
    Action givenAction = forEachOf(
        "Hello", "world", "!"
    ).perform(
        s -> sequential(asList(
            simple(
                "print {s}",
                () -> System.out.println("<" + s.get() + ">")
            ),
            simple(
                "add {s} to 'out'",
                () -> out.add("'" + s.get() + "'")
            )
        ))
    );
    // When
    TestUtils.createReportingActionPerformer(givenAction).perform();
    // Then
    assertThat(
        out,
        TestUtils.allOf(
            TestUtils.<List<String>, String>matcherBuilder()
                .f("0thElement", list -> list.get(0))
                .p("=='Hello'", s -> Objects.equals(s, "'Hello'")),
            TestUtils.<List<String>, String>matcherBuilder()
                .f("1stElement", list -> list.get(1))
                .p("=='world'", s -> Objects.equals(s, "'world'")),
            TestUtils.<List<String>, String>matcherBuilder()
                .f("2ndElement", list -> list.get(2))
                .p("=='!'", s -> Objects.equals(s, "'!'"))
        ));
  }


  @Test
  public void givenConcurrentForEachAction$whenPerformWithReporting$worksCorrectly() throws InterruptedException {
    List<String> out = Collections.synchronizedList(new LinkedList<>());
    // Given
    Action givenAction = forEachOf(
        "Hello", "world", "!"
    ).concurrently(
    ).perform(
        s -> sequential(
            simple(
                "print {s}",
                () -> System.out.println("<" + s.get() + ">")
            ),
            simple(
                "add {s} to 'out'",
                () -> out.add("'" + s.get() + "'")
            )
        )
    );
    // When3
    new ReportingActionPerformer.Builder(givenAction).to(Writer.Std.ERR).build().perform();
    // Then
    assertThat(
        out,
        TestUtils.allOf(
            TestUtils.<List<String>, List<String>>matcherBuilder()
                .f("passthrough", list -> list)
                .p("contains:'Hello'", s -> s.contains("'Hello'")),
            TestUtils.<List<String>, List<String>>matcherBuilder()
                .f("passthrough", list -> list)
                .p("contains:'world'", s -> s.contains("'world'")),
            TestUtils.<List<String>, List<String>>matcherBuilder()
                .f("passthrough", list -> list)
                .p("contains:'!'", s -> s.contains("'!'"))
        ));
  }


  @Test(expected = IllegalStateException.class)
  public void givenConfusingForEachAction$whenPerformWithReporting$worksCorrectly() {
    // Given
    Action givenAction = forEachOf(
        "Hello", "world", "!"
    ).sequentially(
    ).perform(
        s -> concurrent(asList(
            nop("YOU CANNOT CREATE ACTIONS OF THE SAME NAME UNDER ONE forEachOf ACTION"),
            nop("YOU CANNOT CREATE ACTIONS OF THE SAME NAME UNDER ONE forEachOf ACTION"),
            nop(),
            nop()
        ))
    );
    // When
    try {
      TestUtils.createReportingActionPerformer(givenAction).perform();
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
}
