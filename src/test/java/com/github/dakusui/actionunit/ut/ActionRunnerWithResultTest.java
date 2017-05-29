package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.helpers.Builders;
import com.github.dakusui.actionunit.visitors.ActionPrinter;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import com.github.dakusui.actionunit.visitors.ReportingActionRunner;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.function.Function;

import static com.github.dakusui.actionunit.helpers.Actions.*;
import static com.github.dakusui.actionunit.helpers.Builders.attempt;
import static com.github.dakusui.actionunit.helpers.Builders.forEachOf;
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
    protected ActionRunner createRunner() {
      return new ActionRunner.Impl(5);
    }

    @Override
    public ActionPrinter getPrinter(ReportingActionRunner.Writer writer) {
      return new ActionPrinter.Impl(ReportingActionRunner.Writer.Std.ERR);
    }

    void performAndPrintAction(Action action) {
      new ReportingActionRunner.Builder(action).to(getWriter()).build().perform();
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
      performAndPrintAction(action);
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
      performAndPrintAction(action);
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
        performAndPrintAction(action);
        throw new IllegalStateException("This path mustn't be executed since the action should fail.");
      } catch (AssertionError e) {
        action.accept(this.getPrinter());
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
    }

    @Test
    public void givenErrorAction$whenRunAndPrinted$thenPrintedCorrectly() {
      ////
      // Given simple action
      Action action = createErrorAction();
      ////
      //When performed and printed
      try {
        performAndPrintAction(action);
        throw new IllegalStateException("This pass mustn't be executed since the action should fail.");
      } catch (RuntimeException e) {
        ////
        //Then printed correctly
        //noinspection unchecked
        assertThat(getWriter(),
            allOf(
                hasItemAt(0, startsWith("(E)An error action")),
                hasItemAt(1, equalTo("  (E)This gives a runtime exception always"))
            ));
      }
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
      performAndPrintAction(action);
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
      Action action = forEachOf(
          asList("ItemA", "ItemB", "ItemC")
      ).perform(
          i -> sequential(
              simple("Sink-1", () -> {
              }),
              simple("Sink-2", () -> {
              })
          )
      );
      ////
      //When performed and printed
      performAndPrintAction(action);
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
      Action action = forEachOf(
          asList("ItemA", "ItemB", "ItemC")
      ).perform(
          i -> sequential(
              simple("Sink-1", () -> {
                throw new RuntimeException("Failing");
              }),
              simple("Sink-2", () -> {
              })));
      ////
      //When performed and printed
      try {
        performAndPrintAction(action);
      } finally {
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

  public static class AttemptAction extends Base {
    @Test
    public void givenPassingAttemptAction$whenPerformed$thenWorksFine() {
      Action action = attempt(
          nop()
      ).recover(
          Exception.class,
          e -> nop()
      ).ensure(
          nop()
      );
      performAndPrintAction(action);
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
      Action action = attempt(
          simple("Howdy, NPE", new Runnable() {
            @Override
            public void run() {
              throw new NullPointerException(this.toString());
            }
          })
      ).recover(
          NullPointerException.class,
          e -> nop()
      ).ensure(
          nop()
      );
      performAndPrintAction(action);
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

  public static class TestTest extends Base {
    @Test
    public void givenTestAction$whenPerformed$thenWorksFine() {
      Action action = Builders.<String, Integer>given("HelloTestCase", () -> "World")
          .when("",
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
          .then("", v -> v == 5);
      performAndPrintAction(action);
      assertThat(
          this.getWriter().get(0),
          equalTo("(+)HelloTestCase"));
      assertThat(
          this.getWriter().get(1),
          equalTo("  Given:World"));
      assertThat(
          this.getWriter().get(2),
          equalTo("  CompatWhen:Function(length)"));
      assertThat(
          this.getWriter().get(3),
          equalTo("  Then:[Matcher(<5>)]"));
    }


    @Test(expected = AssertionError.class)
    public void givenFailingAction$whenPerformed$thenWorksFine() {
      Action action = Builders.<String, Integer>given("HelloTestCase", () -> "World")
          .when(
              "length",
              input -> input.length() + 1)
          .then("equals to 5", integer -> integer.equals(5));
      try {
        performAndPrintAction(action);
      } finally {
        ////
        //Then:
        //  Expectation is to get 0 and therefore AssertionError will be thrown.
        //  If we use assertXyz method here and if the output to the printer does
        //  not match, the AssertionError will be thrown, which confuses JUnit and users.
        //  Thus, here we are going to throw IllegalStateException.
        //noinspection unchecked
        if (!getWriter().get(0).equals("(F)HelloTestCase") ||
            !getWriter().get(1).equals("  Given:World") ||
            !getWriter().get(2).equals("  CompatWhen:Function(length)") ||
            !getWriter().get(3).startsWith("  Then:[Matcher(<5>)]")) {
          //noinspection ThrowFromFinallyBlock
          throw new IllegalStateException();
        }
      }
    }
  }
}
