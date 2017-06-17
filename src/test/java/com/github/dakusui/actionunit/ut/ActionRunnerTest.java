package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.actions.HandlerFactory;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.PrintingActionScanner;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.function.Supplier;

import static com.github.dakusui.actionunit.utils.TestUtils.hasItemAt;
import static java.util.Arrays.asList;
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
      return PrintingActionScanner.Factory.DEFAULT_INSTANCE.create(writer);
    }
  }

  public static class DoubleCompatForEach extends Base {
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
      return forEachOf(asList("A", "B")).perform(
          new HandlerFactory.Base<String>() {
            @Override
            protected Action create(Supplier<String> i) {
              return sequential(
                  simple("Prefix with 'outer-'", () -> getWriter().writeLine("outer-" + i.get())),
                  forEachOf("a", "b").perform(
                      new HandlerFactory.Base<String>() {
                        @Override
                        public Action create(Supplier<String> j) {
                          return simple("Prefix with '\\_inner-'", () -> getWriter().writeLine("\\_inner-" + j.get()));
                        }
                      }
                  ),
                  simple("Prefix with 'outer-'", () -> getWriter().writeLine("outer-" + i.get()))
              );

            }
          }
      );
    }
  }
}
