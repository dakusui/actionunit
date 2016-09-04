package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.actionunit.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static com.github.dakusui.actionunit.Actions.*;
import static com.github.dakusui.actionunit.utils.TestUtils.hasItemAt;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertTrue;

@RunWith(Enclosed.class)
public class ActionRunnerWithResultTest {

  static abstract class Base {
    final TestUtils.Out           out    = new TestUtils.Out();
    final ActionRunner.WithResult runner = new ActionRunner.WithResult();

    public Action createPassingAction(final int durationInMilliseconds) {
      return simple("A passing action", new Runnable() {
        @Override
        public void run() {
          try {
            TimeUnit.MICROSECONDS.sleep(durationInMilliseconds);
          } catch (InterruptedException e) {
            throw ActionException.wrap(e);
          }
        }

        @Override
        public String toString() {
          return "This passes always";
        }
      });
    }

    public Action createPassingAction() {
      return createPassingAction(0);
    }

    public Action createFailingAction() {
      return simple("A failing action", new Runnable() {
        @Override
        public void run() {
          assertTrue("Expected failure", false);
        }

        @Override
        public String toString() {
          return "This fails always";
        }
      });
    }

    public Action createErrorAction() {
      return simple("An error action", new Runnable() {
        @Override
        public void run() {
          throw new RuntimeException("Expected runtime exception");
        }

        @Override
        public String toString() {
          return "This gives a runtime exception always";
        }
      });
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
      action.accept(runner.createPrinter(out));
      ////
      //Then printed correctly
      //noinspection unchecked
      assertThat(out,
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
      action.accept(runner);
      action.accept(runner.createPrinter(out));
      ////
      //Then printed correctly
      //noinspection unchecked
      assertThat(out,
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
        action.accept(runner);
        throw new IllegalStateException("This pass mustn't be executed since the action should fail.");
      } catch (AssertionError e) {
        action.accept(runner.createPrinter(out));
      }
      ////
      //Then printed correctly
      //noinspection unchecked
      assertThat(out,
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
        action.accept(runner);
        throw new IllegalStateException("This pass mustn't be executed since the action should fail.");
      } catch (RuntimeException e) {
        action.accept(runner.createPrinter(out));
      }
      ////
      //Then printed correctly
      //noinspection unchecked
      assertThat(out, allOf(
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
      action.accept(runner);
      action.accept(runner.createPrinter(out));
      ////
      //Then printed correctly
      //noinspection unchecked
      assertThat(out, allOf(
          hasItemAt(0, equalTo("(+)Concurrent (3 actions)")),
          hasItemAt(1, equalTo("  (+)A passing action")),
          hasItemAt(2, equalTo("    (+)This passes always")),
          hasItemAt(3, equalTo("  (+)A passing action")),
          hasItemAt(4, equalTo("    (+)This passes always")),
          hasItemAt(5, equalTo("  (+)A passing action")),
          hasItemAt(6, equalTo("    (+)This passes always"))
      ));
      assertThat(out, hasSize(7));
    }
  }

  public static class ForEachAction extends Base {
    @Test
    public void givenConcurrentAction$whenPerformed$thenWorksFine() {
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
      action.accept(runner);
      action.accept(runner.createPrinter(out));
      ////
      //Then printed correctly
      //noinspection unchecked
      assertThat(out, allOf(
          hasItemAt(0, equalTo("(+)ForEach (Sequential, 3 items) {Sink-1,Sink-2}")),
          hasItemAt(1, equalTo("  (+)Sequential (2 actions)")),
          hasItemAt(2, equalTo("    (+)Tag(0)")),
          hasItemAt(3, equalTo("    (+)Tag(1)"))
      ));
      assertThat(out, hasSize(4));
    }
  }

}
