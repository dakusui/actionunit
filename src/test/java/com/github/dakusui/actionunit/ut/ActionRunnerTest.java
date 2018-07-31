package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.ut.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.ActionPrinter;
import com.github.dakusui.crest.Crest;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.stream.Stream;

import static com.github.dakusui.actionunit.core.ActionSupport.*;
import static com.github.dakusui.crest.Crest.asInteger;
import static com.github.dakusui.crest.Crest.asString;

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
        Crest.assertThat(
            getWriter(),
            Crest.allOf(
                asString("get", 0).equalTo("outer-A").$(),
                asString("get", 1).equalTo("\\_inner-a").$(),
                asString("get", 2).equalTo("\\_inner-b").$(),
                asString("get", 3).equalTo("outer-A").$(),
                asString("get", 4).equalTo("outer-B").$(),
                asString("get", 5).equalTo("\\_inner-a").$(),
                asString("get", 6).equalTo("\\_inner-b").$(),
                asString("get", 7).equalTo("outer-B").$(),
                asInteger("size").equalTo(8).$()
            ));
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
