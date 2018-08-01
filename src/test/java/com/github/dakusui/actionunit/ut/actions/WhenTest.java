package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.ut.utils.TestUtils;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static com.github.dakusui.actionunit.core.ActionSupport.*;
import static com.github.dakusui.crest.Crest.asListOf;
import static com.github.dakusui.crest.Crest.assertThat;

public class WhenTest extends TestUtils.TestBase {
  @Test
  public void test() {
    Action action = forEach(
        "v",
        (c) -> Stream.of(1, 2, 3, 4)
    ).perform(
        when(
            c -> v(c) > 2
        ).perform(
            simple(
                "hello",
                (c) -> System.out.println("hello" + v(c))
            )
        ).build()
    );
    ReportingActionPerformer.create(Writer.Std.OUT).performAndReport(action);
  }

  @Test
  public void givenOneValue$when_MatchingWhen_$thenWorksFine() {
    List<String> out = new LinkedList<>();
    Action action = when(
        c -> true
    ).perform(
        simple("meets", (c) -> out.add("Condition met"))
    ).otherwise(
        simple("not meets", (c) -> out.add("Condition not met"))
    );
    ReportingActionPerformer.create(Writer.Std.OUT).performAndReport(action);
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
        c -> false
    ).perform(
        simple("meets", (c) -> out.add("Condition met"))
    ).otherwise(
        simple("not meets", (c) -> out.add("Condition not met"))
    );
    ReportingActionPerformer.create(Writer.Std.OUT).performAndReport(action);

    assertThat(
        out,
        asListOf(String.class).containsExactly(
            Collections.singleton("Condition not met")
        ).$()
    );
  }

  private static int v(Context c) {
    return c.valueOf("v");
  }
}
