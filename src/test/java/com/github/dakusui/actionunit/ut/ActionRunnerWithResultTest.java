package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.CompatActions;
import com.github.dakusui.actionunit.Context;
import com.github.dakusui.actionunit.compat.Piped;
import com.github.dakusui.actionunit.connectors.Pipe;
import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.actionunit.connectors.Source;
import com.github.dakusui.actionunit.visitors.ActionPrinter;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.function.Function;

import static com.github.dakusui.actionunit.Actions.*;
import static com.github.dakusui.actionunit.utils.TestUtils.hasItemAt;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;

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
              hasItemAt(0, startsWith("(F)A failing action")),
              hasItemAt(1, equalTo("  (F)This fails always"))
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
          hasItemAt(0, startsWith("(E)An error action")),
          hasItemAt(1, equalTo("  (E)This gives a runtime exception always"))
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

  public static class CompatForEachAction extends Base {
    @Test
    public void givenPassingConcurrentAction$whenPerformed$thenWorksFine() {
      ////
      // Given concurrent action
      Action action = CompatActions.foreach(
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
          hasItemAt(0, equalTo("(+)CompatForEach (Sequential, 3 items) {Sink-1,Sink-2}")),
          hasItemAt(1, equalTo("  (+)Sequential (2 actions); 3 times")),
          hasItemAt(2, equalTo("    (+)Tag(0); 3 times")),
          hasItemAt(3, equalTo("    (+)Tag(1); 3 times"))
      ));
      assertThat(getWriter(), hasSize(4));
    }

    @Test(expected = RuntimeException.class)
    public void givenFailingConcurrentAction$whenPerformed$thenWorksFine() {
      ////
      // Given concurrent action
      Action action = CompatActions.foreach(
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
            hasItemAt(0, startsWith("(E)CompatForEach (Sequential, 3 items) {Sink-1,Sink-2}")),
            hasItemAt(1, startsWith("  (E)Sequential (2 actions)")),
            hasItemAt(2, startsWith("    (E)Tag(0)")),
            hasItemAt(3, startsWith("    ( )Tag(1)"))
        ));
        assertThat(getWriter(), hasSize(4));
      }
    }
  }

  public static class CompatAttemptAction extends Base {
    @Test
    public void givenPassingAttemptAction$whenPerformed$thenWorksFine() {
      Action action = CompatActions.attempt(
          nop()
      ).recover(
          nop()
      ).ensure(
          nop()
      ).build();
      action.accept(this.getRunner());
      action.accept(this.getPrinter());
      assertThat(getWriter(), allOf(
          hasItemAt(0, equalTo("(+)CompatAttempt")),
          hasItemAt(1, equalTo("  (+)(nop)")),
          hasItemAt(2, equalTo("  ( )Recover")),
          hasItemAt(3, equalTo("    ( )(nop)")),
          hasItemAt(4, equalTo("  (+)Ensure")),
          hasItemAt(5, equalTo("    (+)(nop)"))
      ));
      assertThat(getWriter(), hasSize(6));
    }

    @Test
    public void givenFailingAttemptAction$whenPerformed$thenWorksFine() {
      Action action = CompatActions.attempt(
          simple(new Runnable() {
            @Override
            public void run() {
              throw new NullPointerException(this.toString());
            }

            @Override
            public String toString() {
              return "Howdy, NPE";
            }
          })
      ).recover(
          NullPointerException.class,
          nop()
      ).ensure(
          nop()
      ).build();
      action.accept(this.getRunner());
      action.accept(this.getPrinter());
      assertThat(getWriter(), allOf(
          hasItemAt(0, equalTo("(+)CompatAttempt")),
          hasItemAt(1, equalTo("  (E)Howdy, NPE")),
          hasItemAt(2, equalTo("  (+)Recover")),
          hasItemAt(3, equalTo("    (+)(nop)")),
          hasItemAt(4, equalTo("  (+)Ensure")),
          hasItemAt(5, equalTo("    (+)(nop)"))
      ));
      assertThat(getWriter(), hasSize(6));
    }
  }

  public static class TagTest extends Base {
    @Test(expected = UnsupportedOperationException.class)
    public void givenTagAction$whenPerformed$thenWorksFine() {
      Action action = tag(0);
      action.accept(this.getRunner());
      action.accept(this.getPrinter());
    }
  }

  public static class PipedTest extends Base {
    @Test
    public void givenPiped$whenPerformed$thenWorksFine() {
      Action action = CompatActions.pipe(
          new Source<String>() {
            @Override
            public String apply(Context context) {
              return "Hello";
            }
          },
          new Pipe<String, Integer>() {
            @Override
            public Integer apply(String input, Context context) {
              return input.length();
            }
          },
          new Sink<Integer>() {
            @Override
            public void apply(Integer input, Context context) {
              getWriter().writeLine("<<" + input.toString() + ">>");
            }
          }
      );
      action.accept(this.getRunner());
      action.accept(this.getPrinter());
    }
  }

  public static class TestTest extends Base {
    @Test
    public void givenTestAction$whenPerformed$thenWorksFine() {
      Action action = CompatActions.<String, Integer>test("HelloTestCase")
          .given("World")
          .when(
              new Function<String, Integer>() {
                @Override
                public Integer apply(String input) {
                  return input.length();
                }

                @Override
                public String toString() {
                  return "length";
                }
              })
          .then(equalTo(5)).build();
      action.accept(this.getRunner());
      action.accept(this.getPrinter());
      assertThat(
          this.getWriter().get(0),
          equalTo("(+)HelloTestCase"));
      assertThat(
          this.getWriter().get(1),
          equalTo("  Given:World"));
      assertThat(
          this.getWriter().get(2),
          equalTo("  When:Function(length)"));
      assertThat(
          this.getWriter().get(3),
          equalTo("  Then:[Matcher(<5>)]"));

      //noinspection unchecked
      Piped<String, Integer> piped = (Piped<String, Integer>) action;
      assertEquals(1, piped.getDestinationSinks().length);
      assertEquals("Function(length)", piped.getPipe().toString());
    }


    @Test(expected = AssertionError.class)
    public void givenFailingAction$whenPerformed$thenWorksFine() {
      Action action = CompatActions.<String, Integer>test("HelloTestCase")
          .given("World")
          .when(
              new Function<String, Integer>() {
                @Override
                public Integer apply(String input) {
                  return input.length() + 1;
                }

                public String toString() {
                  return "length";
                }
              })
          .then(equalTo(5)).build();
      try {
        action.accept(this.getRunner());
      } finally {
        action.accept(this.getPrinter());

        ////
        //Then:
        //  Expectation is to get 0 and therefore AssertionError will be thrown.
        //  If we use assertXyz method here and if the output to the printer does
        //  not match, the AssertionError will be thrown, which confuses JUnit and users.
        //  Thus, here we are going to throw IllegalStateException.
        //noinspection unchecked
        if (!getWriter().get(0).equals("(F)HelloTestCase") ||
            !getWriter().get(1).equals("  Given:World") ||
            !getWriter().get(2).equals("  When:Function(length)") ||
            !getWriter().get(3).startsWith("  Then:[Matcher(<5>)]")) {
          //noinspection ThrowFromFinallyBlock
          throw new IllegalStateException();
        }
      }
    }
  }
}
