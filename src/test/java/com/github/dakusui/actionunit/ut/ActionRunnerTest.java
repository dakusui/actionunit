package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.compat.utils.TestUtils;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.visitors.ActionPrinter;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.stream.Stream;

import static com.github.dakusui.actionunit.compat.utils.TestUtils.hasItemAt;
import static com.github.dakusui.actionunit.core.ActionSupport.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(Enclosed.class)
public class ActionRunnerTest {
  public abstract static class Base extends ActionRunnerTestBase {
    @Override
    protected Action.Visitor createRunner() {
      return TestUtils.createActionPerformer();
    }

    @Override
    public Action.Visitor getPrinter(Writer writer) {
      return new ActionPrinter(writer);
    }
  }

  public static class DoubleCompatCompatForEach extends Base {
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
        getWriter().forEach(System.out::println);
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
      return forEach(
          "i",
          () -> Stream.of("A", "B")
      ).perform(
          sequential(
              simple(
                  "Prefix env 'outer-'",
                  (c) -> getWriter().writeLine("outer-" + c.valueOf("i"))
              ),
              forEach(
                  "j",
                  () -> Stream.of("a", "b")
              ).perform(
                  simple(
                      "Prefix env '\\_inner-'",
                      (cc) -> getWriter().writeLine("\\_inner-" + cc.valueOf("j"))
                  )
              ),
              simple(
                  "Prefix env 'outer-'",
                  (cc) -> getWriter().writeLine("outer-" + cc.valueOf("i"))
              )
          )
      );
    }
  }
}
