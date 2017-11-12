package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.reporting.ReportingActionPerformer;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.github.dakusui.crest.Crest.asListOf;
import static com.github.dakusui.crest.Crest.assertThat;
import static java.util.Arrays.asList;

public class WhenTest extends TestUtils.TestBase implements Context {
  @Test
  public void test() {
    Action action = forEachOf(
        asList(1, 2, 3, 4)
    ).perform(
        ($, v) -> $.when(
            v,
            (Integer input) -> input > 2
        ).perform(
            ($$) -> $$.simple(
                "hello",
                () -> System.out.println("hello" + v.get())
            )
        ).build()
    );
    new ReportingActionPerformer.Builder(action).build().performAndReport();
  }

  @Test
  public void givenOneValue$when_MatchingWhen_$thenWorksFine() {
    List<String> out = new LinkedList<>();
    Action action = when(
        () -> "Hello",
        v -> v.startsWith("H")
    ).perform(
        simple("meets", () -> out.add("Condition met"))
    ).otherwise(
        simple("not meets", () -> out.add("Condition not met"))
    );
    new ReportingActionPerformer.Builder(action).build().performAndReport();

    assertThat(
        out,
        asListOf(String.class).containsExactly(
            Collections.singleton("Condition met")
        ).$()
    );
  }

  @Test
  public void givenOneValue$when_NotMatchingWhen_$thenWorksFine() {
    List<String> out = new LinkedList<>();
    Action action = when(
        () -> "Hello",
        v -> v.startsWith("h")
    ).perform(
        simple("meets", () -> out.add("Condition met"))
    ).otherwise(
        simple("not meets", () -> out.add("Condition not met"))
    );
    new ReportingActionPerformer.Builder(action).build().performAndReport();

    assertThat(
        out,
        asListOf(String.class).containsExactly(
            Collections.singleton("Condition not met")
        ).$()
    );
  }
}
