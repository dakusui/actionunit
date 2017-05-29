package com.github.dakusui.actionunit.compat.actions;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.compat.CompatActions;
import com.github.dakusui.actionunit.compat.connectors.Sink;
import com.github.dakusui.actionunit.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import org.junit.Test;

import static com.github.dakusui.actionunit.helpers.Actions.*;
import static com.github.dakusui.actionunit.utils.TestUtils.hasItemAt;
import static java.util.Arrays.asList;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.junit.MatcherAssert.assertThat;

public class WithTest {
  @Test
  public void givenWithInsideFor$whenPerform$thenWorksFine() {
    final TestUtils.Out out = new TestUtils.Out();
    Action action = CompatActions.foreach(
        asList("A", "B", "C"),
        sequential(
            CompatActions.tag(0),
            CompatActions.with("hello", new Sink.Base<String>() {
              @Override
              protected void apply(String input, Object... outer) {
                out.writeLine("  " + input);
                out.writeLine("  outer:" + asList(outer));
              }
            })
        ),
        new Sink.Base<String>() {

          @Override
          protected void apply(String input, Object... outer) {
            out.writeLine(input);
          }
        }
    );
    boolean succeeded = false;
    try {
      action.accept(new ActionRunner.Impl());
      succeeded = true;
    } finally {
      if (succeeded) {
        assertThat(
            out,
            allOf(
                hasItemAt(0, equalTo("A")),
                hasItemAt(1, equalTo("  hello")),
                hasItemAt(2, equalTo("  outer:[A, (N/A)]")),
                hasItemAt(3, equalTo("B")),
                hasItemAt(4, equalTo("  hello")),
                hasItemAt(5, equalTo("  outer:[B, (N/A)]")),
                hasItemAt(6, equalTo("C")),
                hasItemAt(7, equalTo("  hello")),
                hasItemAt(8, equalTo("  outer:[C, (N/A)]"))
            ));
      }
    }
  }
}
