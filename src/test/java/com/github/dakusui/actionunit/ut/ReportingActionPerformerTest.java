package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.utils.Matchers;
import com.github.dakusui.actionunit.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.ActionPerformer;
import com.github.dakusui.actionunit.visitors.PrintingActionScanner;
import com.github.dakusui.actionunit.visitors.reporting.Report;
import com.github.dakusui.actionunit.visitors.reporting.ReportingActionPerformer;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static com.github.dakusui.actionunit.utils.TestUtils.hasItemAt;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;

@RunWith(Enclosed.class)
public class ReportingActionPerformerTest implements Context {
  public abstract static class Base extends ActionRunnerTestBase<ActionPerformer, PrintingActionScanner> {
    @Override
    protected ActionPerformer createRunner() {
      return TestUtils.createActionPerformer();
    }

    @Override
    public PrintingActionScanner getPrinter(Writer writer) {
      return TestUtils.createPrintingActionScanner(Writer.Std.ERR);
    }

    void performAndPrintAction(Action action) {
      new ReportingActionPerformer.Builder(action).to(getWriter()).with(Report.Record.Formatter.DEBUG_INSTANCE).build().performAndReport();
    }
  }

  public static class SimpleAction extends Base {
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
              hasItemAt(0, equalTo("[o]1-A passing action")),
              hasItemAt(1, equalTo("  [o]0-This passes always"))
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
                hasItemAt(0, startsWith("[x]1-A failing action")),
                hasItemAt(1, equalTo("  [x]0-This fails always"))
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
                hasItemAt(0, startsWith("[x]1-An error action")),
                hasItemAt(1, equalTo("  [x]0-This gives a runtime exception always"))
            ));
      }
    }
  }

  public static class ConcurrentAction extends Base {
    @Test
    public void givenConcurrentAction$whenPerformed$thenWorksFine() {
      ////
      // Given concurrent action
      Action action = concurrent(
          createPassingAction(1, 100),
          createPassingAction(2, 200),
          createPassingAction(3, 300));
      ////
      //When performed and printed
      performAndPrintAction(action);
      ////
      //Then printed correctly
      //noinspection unchecked
      assertThat(getWriter(), allOf(
          hasItemAt(0, equalTo("[o]6-Concurrent (3 actions)")),
          hasItemAt(1, equalTo("  [o]1-A passing action-1")),
          hasItemAt(2, equalTo("    [o]0-This passes always-1")),
          hasItemAt(3, equalTo("  [o]3-A passing action-2")),
          hasItemAt(4, equalTo("    [o]2-This passes always-2")),
          hasItemAt(5, equalTo("  [o]5-A passing action-3")),
          hasItemAt(6, equalTo("    [o]4-This passes always-3"))
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
          ($, i) -> $.sequential(
              $.simple("Sink-1", () -> {
              }),
              $.simple("Sink-2", () -> {
              })
          )
      );
      ////
      //When performed and printed
      performAndPrintAction(action);
      ////
      //Then printed correctly
      //noinspection unchecked
      assertThat(
          getWriter(),
          Matchers.allOf(
              hasItemAt(0, startsWith("[o]0-ForEach (SEQUENTIALLY)")),
              hasItemAt(1, equalTo("  [ooo]2-Sequential (2 actions)")),
              hasItemAt(2, equalTo("    [ooo]0-Sink-1")),
              hasItemAt(3, equalTo("    [ooo]1-Sink-2"))
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
          ($, i) -> $.sequential(
              $.simple("Sink-1", () -> {
                throw new RuntimeException("Failing");
              }),
              $.simple("Sink-2", () -> {
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
            hasItemAt(0, startsWith("[x]0-ForEach")),
            hasItemAt(1, startsWith("  [x]2-Sequential (2 actions)")),
            hasItemAt(2, startsWith("    [x]0-Sink-1")),
            hasItemAt(3, startsWith("    []1-Sink-2"))
        ));
        assertThat(getWriter(), hasSize(4));
      }
    }
  }

  public static class AttemptAction extends Base implements Context {
    @Test
    public void givenPassingAttemptAction$whenPerformed$thenWorksFine() {
      Action action = attempt(
          nop()
      ).recover(
          Exception.class,
          ($, e) -> $.nop()
      ).ensure(
          Context::nop
      );
      performAndPrintAction(action);
      assertThat(getWriter(), allOf(
          hasItemAt(0, equalTo("[o]1-Attempt")),
          hasItemAt(1, equalTo("  [o]0-Target")),
          hasItemAt(2, equalTo("    [o]0-(nop)")),
          hasItemAt(3, equalTo("  []1-Recover(Exception)")),
          hasItemAt(4, equalTo("    []0-(nop)")),
          hasItemAt(5, equalTo("  [o]2-Ensure")),
          hasItemAt(6, equalTo("    [o]0-(nop)"))
      ));
      assertThat(getWriter(), hasSize(7));
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
          ($, e) -> $.nop()
      ).ensure(
          ($) -> $.nop()
      );
      performAndPrintAction(action);
      assertThat(getWriter(), Matchers.allOf(
          hasItemAt(0, equalTo("[o]1-Attempt")),
          hasItemAt(1, equalTo("  [x]0-Target")),
          hasItemAt(2, equalTo("    [x]0-Howdy, NPE")),
          hasItemAt(3, equalTo("  [o]1-Recover(NullPointerException)")),
          hasItemAt(4, equalTo("    [o]0-(nop)")),
          hasItemAt(5, equalTo("  [o]2-Ensure")),
          hasItemAt(6, equalTo("    [o]0-(nop)"))
      ));
      assertThat(getWriter(), hasSize(7));
    }
  }

  public static class TestTest extends Base implements Context {
    @Test
    public void givenTestAction$whenPerformed$thenWorksFine() {
      Action action =
          this.<String, Integer>given("string 'World'", () -> "World")
              .when("length", String::length)
              .then("==5", v -> v == 5);
      performAndPrintAction(action);
      assertThat(
          this.getWriter().get(0),
          allOf(
              containsString("[o]"),
              containsString("TestAction")
          )
      );
      assertThat(
          this.getWriter().get(1),
          equalTo("  [o]0-Given"));
      assertThat(
          this.getWriter().get(2),
          equalTo("    [o]0-string 'World'"));
      assertThat(
          this.getWriter().get(3),
          equalTo("  [o]1-When"));
      assertThat(
          this.getWriter().get(4),
          equalTo("    [o]0-length"));
      assertThat(
          this.getWriter().get(5),
          equalTo("  [o]2-Then"));
      assertThat(
          this.getWriter().get(6),
          equalTo("    [o]0-==5"));
    }


    @Test(expected = AssertionError.class)
    public void givenFailingAction$whenPerformed$thenWorksFine() {
      Action action = this.<String, Integer>given("HelloTestCase", () -> "World")
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
        if (!getWriter().get(0).equals("[x]0-TestAction") ||
            !getWriter().get(1).equals("  [o]0-Given") ||
            !getWriter().get(2).equals("    [o]0-HelloTestCase") ||
            !getWriter().get(3).equals("  [o]1-When") ||
            !getWriter().get(4).equals("    [o]0-length") ||
            !getWriter().get(5).equals("  [x]2-Then") ||
            !getWriter().get(6).equals("    [x]0-equals to 5")) {
          getWriter().forEach(System.err::println);
          //noinspection ThrowFromFinallyBlock
          throw new IllegalStateException();
        }
      }
    }
  }
}
