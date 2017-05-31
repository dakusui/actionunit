package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.helpers.Actions2;
import com.github.dakusui.actionunit.helpers.Builders2;
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

public class WhileTest implements Actions2, Builders2 {
  @Test
  public void test() {
    final TestUtils.Out out = new TestUtils.Out();
    Action action = whilst(
        toSupplier(new AtomicInteger(0)),
        i -> i.get() < 4
    ).perform(
        i -> simple("Say 'Hello'",
            () -> {
              out.writeLine("Hello");
              i.get().getAndIncrement();
            }
        )
    ).$();
    final TestUtils.Out result = new TestUtils.Out();
    new ReportingActionPerformer.Builder(action).to(result).build().perform();
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
