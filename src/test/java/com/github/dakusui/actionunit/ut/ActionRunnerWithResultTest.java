package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.actionunit.visitors.ActionPrinter;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static com.github.dakusui.actionunit.Actions.concurrent;
import static com.github.dakusui.actionunit.Actions.forEach;
import static com.github.dakusui.actionunit.utils.TestUtils.hasItemAt;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;

@RunWith(Enclosed.class)
public class ActionRunnerWithResultTest {
  public abstract static class Base extends ActionRunnerTestBase {
    @Override
    protected ActionRunner.WithResult createRunner() {
      return new ActionRunner.WithResult();
    }

    @Override
    public ActionPrinter getPrinter(ActionPrinter.Writer writer) {
      return ((ActionRunner.WithResult) getRunner()).createPrinter(writer);
    }
  }

  public static class SimpleAction extends Base {
    @Test
    public void givenPassingAction$whenSimplyPrinted$thenPrintedCorrectly() {
      ////
      // Given simple action
      Action action = createPassingAction();
      ////
      //When printed (without being run)
      action.accept(getPrinter());
      ////
      //Then printed correctly
      //noinspection unchecked
      assertThat(getWriter(),
          allOf(
              hasItemAt(0, equalTo("( )A passing action")),
              hasItemAt(1, equalTo("  ( )This passes always"))
          )
      );
    }

    @Test
    public void givenPassingAction$whenRunAndPrinted$thenPrintedCorrectly() {
      ////
      // Given simple action
      Action action = createPassingAction();
      ////
      //When performed and printed.
      action.accept(this.getRunner());
      action.accept(this.getPrinter());
      ////
      //Then printed correctly
      //noinspection unchecked
      assertThat(getWriter(),
          allOf(
              hasItemAt(0, equalTo("(+)A passing action")),
              hasItemAt(1, equalTo("  (+)This passes always"))
          )
      );
    }

    @Test
    public void givenFailingAction$whenRunAndPrinted$thenPrintedCorrectly() {
      ////
      // Given simple action
      Action action = createFailingAction();
      ////
      //When performed and printed
      try {
        action.accept(this.getRunner());
        throw new IllegalStateException("This pass mustn't be executed since the action should fail.");
      } catch (AssertionError e) {
        action.accept(this.getPrinter());
      }
      ////
      //Then printed correctly
      //noinspection unchecked
      assertThat(getWriter(),
          allOf(
              hasItemAt(0, startsWith("(F)A failing action(error=")),
              hasItemAt(1, equalTo("  (F)This fails always(error=Expected failure)"))
          )
      );
    }

    @Test
    public void givenErrorAction$whenRunAndPrinted$thenPrintedCorrectly() {
      ////
      // Given simple action
      Action action = createErrorAction();
      ////
      //When performed and printed
      try {
        action.accept(this.getRunner());
        throw new IllegalStateException("This pass mustn't be executed since the action should fail.");
      } catch (RuntimeException e) {
        action.accept(this.getPrinter());
      }
      ////
      //Then printed correctly
      //noinspection unchecked
      assertThat(getWriter(), allOf(
          hasItemAt(0, startsWith("(E)An error action(error=")),
          hasItemAt(1, equalTo("  (E)This gives a runtime exception always(error=Expected runtime exception)"))
      ));
    }
  }

  public static class ConcurrentAction extends Base {
    @Test
    public void givenConcurrentAction$whenPerformed$thenWorksFine() {
      ////
      // Given concurrent action
      Action action = concurrent(createPassingAction(100), createPassingAction(200), createPassingAction(300));
      ////
      //When performed and printed
      action.accept(this.getRunner());
      action.accept(this.getPrinter());
      ////
      //Then printed correctly
      //noinspection unchecked
      assertThat(getWriter(), allOf(
          hasItemAt(0, equalTo("(+)Concurrent (3 actions)")),
          hasItemAt(1, equalTo("  (+)A passing action")),
          hasItemAt(2, equalTo("    (+)This passes always")),
          hasItemAt(3, equalTo("  (+)A passing action")),
          hasItemAt(4, equalTo("    (+)This passes always")),
          hasItemAt(5, equalTo("  (+)A passing action")),
          hasItemAt(6, equalTo("    (+)This passes always"))
      ));
      assertThat(getWriter(), hasSize(7));
    }
  }

  public static class ForEachAction extends Base {
    @Test
    public void givenPassingConcurrentAction$whenPerformed$thenWorksFine() {
      ////
      // Given concurrent action
      Action action = forEach(
          asList("ItemA", "ItemB", "ItemC"),
          new Sink.Base<String>() {
            @Override
            protected void apply(String input, Object... outer) {
            }

            @Override
            public String toString() {
              return "Sink-1";
            }
          },
          new Sink.Base<String>() {
            @Override
            protected void apply(String input, Object... outer) {
            }

            @Override
            public String toString() {
              return "Sink-2";
            }
          }
      );
      ////
      //When performed and printed
      action.accept(this.getRunner());
      action.accept(this.getPrinter());
      ////
      //Then printed correctly
      //noinspection unchecked
      assertThat(getWriter(), allOf(
          hasItemAt(0, equalTo("(+)ForEach (Sequential, 3 items) {Sink-1,Sink-2}")),
          hasItemAt(1, equalTo("  (+)Sequential (2 actions)")),
          hasItemAt(2, equalTo("    (+)Tag(0)")),
          hasItemAt(3, equalTo("    (+)Tag(1)"))
      ));
      assertThat(getWriter(), hasSize(4));
    }

    @Test(expected = RuntimeException.class)
    public void givenFailingConcurrentAction$whenPerformed$thenWorksFine() {
      ////
      // Given concurrent action
      Action action = forEach(
          asList("ItemA", "ItemB", "ItemC"),
          new Sink.Base<String>() {
            @Override
            protected void apply(String input, Object... outer) {
              throw new RuntimeException("Failing");
            }

            @Override
            public String toString() {
              return "Sink-1";
            }
          },
          new Sink.Base<String>() {
            @Override
            protected void apply(String input, Object... outer) {
            }

            @Override
            public String toString() {
              return "Sink-2";
            }
          }
      );
      ////
      //When performed and printed
      try {
        action.accept(this.getRunner());
      } finally {
        action.accept(this.getPrinter());
        ////
        //Then printed correctly
        //noinspection unchecked
        assertThat(getWriter(), allOf(
            hasItemAt(0, startsWith("(E)ForEach (Sequential, 3 items) {Sink-1,Sink-2}")),
            hasItemAt(1, startsWith("  (E)Sequential (2 actions)")),
            hasItemAt(2, startsWith("    (E)Tag(0)")),
            hasItemAt(3, startsWith("    ( )Tag(1)"))
        ));
        assertThat(getWriter(), hasSize(4));
      }
    }
  }

}
