package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import org.junit.Test;

import java.util.function.Predicate;

import static com.github.dakusui.actionunit.helpers.Actions.repeatwhile;
import static com.github.dakusui.actionunit.compat.CompatActions.simple;
import static com.github.dakusui.actionunit.utils.TestUtils.hasItemAt;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class WhileTest {
  @Test
  public void test() {
    final TestUtils.Out out = new TestUtils.Out();
    Action action = repeatwhile(new Predicate() {
                                  int i = 0;

                                  @Override
                                  public boolean test(Object input) {
                                    return i++ < 4;
                                  }
                                },
        simple(() -> out.writeLine("Hello"))
    );
    ActionRunner.WithResult runner = new ActionRunner.WithResult();
    try {
      action.accept(runner);
    } finally {
      action.accept(runner.createPrinter(new TestUtils.Out()));
    }
    assertThat(out,
        allOf(
            hasItemAt(0, equalTo("Hello")),
            hasItemAt(1, equalTo("Hello")),
            hasItemAt(2, equalTo("Hello")),
            hasItemAt(3, equalTo("Hello"))
        )
    );
    assertEquals(4, out.size());
  }
}
