package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.compat.utils.TestUtils;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.github.dakusui.actionunit.compat.utils.Matchers.allOf;
import static com.github.dakusui.actionunit.core.ActionSupport.*;
import static org.junit.Assert.assertThat;

public class CompatForEachTest extends TestUtils.TestBase {
  @Test
  public void givenForEachAction$whenPerformWithReporting$worksCorrectly() {
    List<String> out = new LinkedList<>();
    // Given
    Action action = forEach(
        "i",
        () -> Stream.of("Hello", "world", "!")
    ).perform(
        sequential(
            simple(
                "print {s}",
                (c) -> System.out.println("<" + c.valueOf("i") + ">")
            ),
            simple(
                "add {s} to 'out'",
                (c) -> out.add("'" + c.valueOf("i") + "'")
            )));
    // When
    TestUtils.createReportingActionPerformer().performAndReport(action);
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
    Action action = forEach(
        "i",
        () -> Stream.of("Hello", "world", "!")
    ).parallelly(
    ).perform(
        sequential(
            simple(
                "print {s}",
                (c) -> System.out.println("<" + c.valueOf("i") + ">")
            ),
            simple(
                "add {s} to 'out'",
                (c) -> out.add("'" + c.valueOf("i") + "'")
            )
        )
    );
    // When3
    ReportingActionPerformer.create(Writer.Std.ERR).performAndReport(action);
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
}
