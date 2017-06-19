package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.reporting.ReportingActionPerformer;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static com.github.dakusui.actionunit.helpers.Utils.toSupplier;
import static com.github.dakusui.actionunit.utils.TestUtils.hasItemAt;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class WhileTest implements Context {
  @Test
  public void test() {
    final TestUtils.Out out = new TestUtils.Out();
    AtomicInteger v = new AtomicInteger(0);
    Action action = whilst(
        toSupplier(v),
        i -> i.get() < 4
    ).perform(
        ($) -> $.simple("Say 'Hello'",
            () -> {
              out.writeLine("Hello");
              v.getAndIncrement();
            }
        )
    );
    final TestUtils.Out result = new TestUtils.Out();
    new ReportingActionPerformer.Builder(action).to(result).build().performAndReport();
    assertThat(out,
        allOf(
            hasItemAt(0, equalTo("Hello")),
            hasItemAt(1, equalTo("Hello")),
            hasItemAt(2, equalTo("Hello")),
            hasItemAt(3, equalTo("Hello"))
        )
    );
    assertEquals(4, out.size());

    assertThat(result,
        allOf(
            hasItemAt(0, equalTo("[o]While")),
            hasItemAt(1, equalTo("  [o...]Say 'Hello'"))
        ));
    assertEquals(2, result.size());
  }
}
