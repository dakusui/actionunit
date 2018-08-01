package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.ut.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import com.github.dakusui.crest.Crest;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static com.github.dakusui.actionunit.core.ActionSupport.forEach;
import static com.github.dakusui.actionunit.core.ActionSupport.sequential;
import static com.github.dakusui.actionunit.core.ActionSupport.simple;
import static com.github.dakusui.crest.Crest.allOf;
import static com.github.dakusui.crest.Crest.asListOf;
import static com.github.dakusui.crest.Crest.asString;
import static com.github.dakusui.crest.Crest.assertThat;

public class ForEachTest extends TestUtils.TestBase {
  @Test
  public void givenForEachAction$whenPerformWithReporting$worksCorrectly() {
    List<String> out = new LinkedList<>();
    // Given
    Action action = forEach(
        "i",
        (c) -> Stream.of("Hello", "world", "!")
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
                asString("get", 0).equalTo("'Hello'").$(),
                asString("get", 1).equalTo("'world'").$(),
                asString("get", 2).equalTo("'!'").$()
        ));
  }


  @Test
  public void givenConcurrentForEachAction$whenPerformWithReporting$worksCorrectly() {
    List<String> out = Collections.synchronizedList(new LinkedList<>());
    // Given
    Action action = forEach(
        "i",
        (c) -> Stream.of("Hello", "world", "!")
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
    Crest.assertThat(
        out,
        Crest.allOf(
		        asListOf(String.class).contains("'Hello'").$(),
		        asListOf(String.class).contains("'world'").$(),
		        asListOf(String.class).contains("'!'").$()
        ));
  }
}
