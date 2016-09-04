package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.actionunit.visitors.ActionPrinter;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static com.github.dakusui.actionunit.Actions.*;
import static com.github.dakusui.actionunit.utils.TestUtils.hasItemAt;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(Enclosed.class)
public class ActionRunnerTest {
  public abstract static class Base extends ActionRunnerTestBase {
    @Override
    protected ActionRunner createRunner() {
      return new ActionRunner.Impl();
    }

    @Override
    public ActionPrinter getPrinter(ActionPrinter.Writer writer) {
      return ActionPrinter.Factory.create(writer);
    }
  }

  public static class DoubleForEach extends Base {
    @Test
    public void givenDoubleForEachAction$whenPerformed$thenExecutedCorrectly() {
      ////
      // given
      Action action = composeAction();
      try {
        ////
        // when
        action.accept(this.getRunner());
      } finally {
        ////
        // then
        //noinspection unchecked
        assertThat(
            getWriter(),
            allOf(
                hasItemAt(0, equalTo("outer-A")),
                hasItemAt(1, equalTo("\\_inner-a")),
                hasItemAt(2, equalTo("\\_inner-b")),
                hasItemAt(3, equalTo("outer-A")),
                hasItemAt(4, equalTo("outer-B")),
                hasItemAt(5, equalTo("\\_inner-a")),
                hasItemAt(6, equalTo("\\_inner-b")),
                hasItemAt(7, equalTo("outer-B"))
            ));
        assertThat(
            getWriter(),
            hasSize(8)
        );
      }
    }

    private Action composeAction() {
      return forEach(asList("A", "B"),
          sequential(
              tag(0),
              forEach(asList("a", "b"), new Sink.Base() {
                @Override
                protected void apply(Object input, Object... outer) {
                  getWriter().writeLine("\\_inner-" + input);
                }
              }),
              tag(0)
          ),
          new Sink.Base() {
            @Override
            protected void apply(Object input, Object... outer) {
              getWriter().writeLine("outer-" + input);
            }
          });
    }
  }
}
