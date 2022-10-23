package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.ut.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.ActionPrinter;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import com.github.dakusui.crest.Crest;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static com.github.dakusui.actionunit.core.ActionSupport.*;
import static com.github.dakusui.actionunit.ut.actions.TestFunctionals.*;
import static com.github.dakusui.actionunit.ut.utils.TestUtils.createReportingActionPerformer;
import static com.github.dakusui.crest.Crest.allOf;
import static com.github.dakusui.crest.Crest.asListOf;
import static com.github.dakusui.crest.Crest.asString;
import static com.github.dakusui.crest.Crest.assertThat;
import static com.github.dakusui.pcond.forms.Predicates.lt;

public class ForEachTest extends TestUtils.TestBase {
  @Test
  public void givenForEachAction$whenPerformWithReporting$worksCorrectly() {
    List<String> out = new LinkedList<>();
    // Given
    Action action = forEach("i", (c) -> Stream.of("Hello", "world", "!")).perform(b ->
        sequential(
            simple("print {s}", (c) -> System.out.println("<" + b.contextVariable(c) + ">")),
            simple("add {s} to 'out'", (c) -> out.add("'" + b.contextVariable(c) + "'"))));
    // When
    createReportingActionPerformer().performAndReport(action, Writer.Std.OUT);
    // Then
    assertThat(out, allOf(
        asString("get", 0).equalTo("'Hello'").$(),
        asString("get", 1).equalTo("'world'").$(),
        asString("get", 2).equalTo("'!'").$()
    ));
  }

  @Test
  public void givenForEach2Action$whenPerformWithReporting$worksCorrectly() {
    List<String> out = new LinkedList<>();
    // Given
    Action action = forEach(constant(Stream.of("Hello", "world", "!")))
        .action(b -> sequential(
            b.toAction(printVariable()),
            b.toAction(v -> out.add("'" + v + "'"))))
        .$();
    // When
    createReportingActionPerformer().performAndReport(action, Writer.Std.OUT);
    // Then
    assertThat(out, allOf(
        asString("get", 0).equalTo("'Hello'").$(),
        asString("get", 1).equalTo("'world'").$(),
        asString("get", 2).equalTo("'!'").$()
    ));
  }


  @Test
  public void printActionTree_6() {
    Action withAction = with(constant(1)).action(
            b -> repeatWhile(b.predicate(lt(10)))
                .perform(sequential(
                    b.toAction(printVariable()),
                    b.updateContextVariableWith(increment())
                )))
        .build(printVariable());
    withAction.accept(new ActionPrinter(Writer.Std.OUT));
    createReportingActionPerformer().performAndReport(withAction, Writer.Std.OUT);
  }


  @Test
  public void givenConcurrentForEachAction$whenPerformWithReporting$worksCorrectly() {
    List<String> out = Collections.synchronizedList(new LinkedList<>());
    // Given
    Action action = forEach(
        "i",
        (c) -> Stream.of("Hello", "world", "!"))
        .parallely()
        .perform(b ->
            sequential(
                simple(
                    "print {s}",
                    (c) -> System.out.println("<" + b.contextVariable(c) + ">")),
                simple(
                    "add {s} to 'out'",
                    (c) -> out.add("'" + b.contextVariable(c) + "'"))
            )
        );
    // When3
    ReportingActionPerformer.create().performAndReport(
        action,
        Writer.Std.ERR
    );
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
