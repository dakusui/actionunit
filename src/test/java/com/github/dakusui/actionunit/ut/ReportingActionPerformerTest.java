package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.ActionSupport;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.ActionPerformer;
import com.github.dakusui.actionunit.visitors.PrintingActionScanner;
import com.github.dakusui.actionunit.visitors.reporting.ReportingActionPerformer;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static com.github.dakusui.actionunit.utils.TestUtils.hasItemAt;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;

@RunWith(Enclosed.class)
public class ReportingActionPerformerTest {
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
      new ReportingActionPerformer.Builder(action).to(getWriter()).build().performAndReport();
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
              hasItemAt(0, equalTo("[o]A passing action")),
              hasItemAt(1, equalTo("  [o]This passes always"))
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
                hasItemAt(0, startsWith("[x]A failing action")),
                hasItemAt(1, equalTo("  [x]This fails always"))
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
                hasItemAt(0, startsWith("[x]An error action")),
                hasItemAt(1, equalTo("  [x]This gives a runtime exception always"))
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
          hasItemAt(0, equalTo("[o]Concurrent (3 actions)")),
          hasItemAt(1, equalTo("  [o]A passing action-1")),
          hasItemAt(2, equalTo("    [o]This passes always-1")),
          hasItemAt(3, equalTo("  [o]A passing action-2")),
          hasItemAt(4, equalTo("    [o]This passes always-2")),
          hasItemAt(5, equalTo("  [o]A passing action-3")),
          hasItemAt(6, equalTo("    [o]This passes always-3"))
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
      assertThat(
          getWriter(),
          TestUtils.allOf(
              hasItemAt(0, startsWith("[o]ForEach (SEQUENTIALLY)")),
              hasItemAt(1, equalTo("  [ooo]Sequential (2 actions)")),
              hasItemAt(2, equalTo("    [ooo]Sink-1")),
              hasItemAt(3, equalTo("    [ooo]Sink-2"))
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
            hasItemAt(0, startsWith("[x]ForEach")),
            hasItemAt(1, startsWith("  [x]Sequential (2 actions)")),
            hasItemAt(2, startsWith("    [x]Sink-1")),
            hasItemAt(3, startsWith("    []Sink-2"))
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
          hasItemAt(0, equalTo("[o]Attempt")),
          hasItemAt(1, equalTo("  [o](nop)")),
          hasItemAt(2, equalTo("  []Recover(Exception)")),
          hasItemAt(3, equalTo("    [](nop)")),
          hasItemAt(4, equalTo("  [o]Ensure")),
          hasItemAt(5, equalTo("    [o](nop)"))
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
      assertThat(getWriter(), TestUtils.allOf(
          hasItemAt(0, equalTo("[o]Attempt")),
          hasItemAt(1, equalTo("  [x]Howdy, NPE")),
          hasItemAt(2, equalTo("  [o]Recover(NullPointerException)")),
          hasItemAt(3, equalTo("    [o](nop)")),
          hasItemAt(4, equalTo("  [o]Ensure")),
          hasItemAt(5, equalTo("    [o](nop)"))
      ));
      assertThat(getWriter(), hasSize(6));
    }
  }

  public static class TestTest extends Base {
    @Test
    public void givenTestAction$whenPerformed$thenWorksFine() {
      Action action =
          ActionSupport.<String, Integer>given("string 'World'", () -> "World")
              .when("length", String::length)
              .then("==5", v -> v == 5);
      performAndPrintAction(action);
      assertThat(
          this.getWriter().get(0),
          equalTo("[o]TestAction"));
      assertThat(
          this.getWriter().get(1),
          equalTo("  [o]Given"));
      assertThat(
          this.getWriter().get(2),
          equalTo("    [o]string 'World'"));
      assertThat(
          this.getWriter().get(3),
          equalTo("  [o]When"));
      assertThat(
          this.getWriter().get(4),
          equalTo("    [o]length"));
      assertThat(
          this.getWriter().get(5),
          equalTo("  [o]Then"));
      assertThat(
          this.getWriter().get(6),
          equalTo("    [o]==5"));
    }


    @Test(expected = AssertionError.class)
    public void givenFailingAction$whenPerformed$thenWorksFine() {
      Action action = ActionSupport.<String, Integer>given("HelloTestCase", () -> "World")
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
        if (!getWriter().get(0).equals("[x]TestAction") ||
            !getWriter().get(1).equals("  [o]Given") ||
            !getWriter().get(2).equals("    [o]HelloTestCase") ||
            !getWriter().get(3).equals("  [o]When") ||
            !getWriter().get(4).equals("    [o]length") ||
            !getWriter().get(5).equals("  [x]Then") ||
            !getWriter().get(6).equals("    [x]equals to 5")) {
          getWriter().forEach(System.err::println);
          //noinspection ThrowFromFinallyBlock
          throw new IllegalStateException();
        }
      }
    }
  }
}
