package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.compat.utils.Matchers;
import com.github.dakusui.actionunit.compat.utils.TestUtils;
import com.github.dakusui.actionunit.n.core.Action;
import com.github.dakusui.actionunit.n.core.Context;
import com.github.dakusui.actionunit.n.core.ContextConsumer;
import com.github.dakusui.actionunit.n.io.Writer;
import com.github.dakusui.actionunit.n.visitors.ActionPerformer;
import com.github.dakusui.actionunit.n.visitors.ActionPrinter;
import com.github.dakusui.actionunit.n.visitors.ReportingActionPerformer;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.stream.Stream;

import static com.github.dakusui.actionunit.compat.utils.TestUtils.hasItemAt;
import static com.github.dakusui.actionunit.n.core.ActionSupport.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;

@RunWith(Enclosed.class)
public class ReportingActionPerformerTest extends TestUtils.TestBase {
  public abstract static class Base extends ActionRunnerTestBase<ActionPerformer, ActionPrinter> {
    @Override
    protected ActionPerformer createRunner() {
      return TestUtils.createActionPerformer();
    }

    @Override
    public ActionPrinter getPrinter(Writer writer) {
      return TestUtils.createPrintingActionScanner(Writer.Std.ERR);
    }

    void performAndPrintAction(Action action) {
      ReportingActionPerformer.create(getWriter()).performAndReport(action);
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
              hasItemAt(0, equalTo("[.]1-A passing action")),
              hasItemAt(1, equalTo("  [.]0-This passes always"))
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
                hasItemAt(0, startsWith("[F]1-A failing action")),
                hasItemAt(1, equalTo("  [F]0-This fails always"))
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
                hasItemAt(0, startsWith("[E]1-An error action")),
                hasItemAt(1, equalTo("  [E]0-This gives a runtime exception always"))
            ));
      }
    }
  }

  public static class ConcurrentAction extends Base {
    @Test
    public void givenConcurrentAction$whenPerformed$thenWorksFine() {
      ////
      // Given concurrent action
      Action action = parallel(
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
          hasItemAt(0, equalTo("[.]6-Concurrent (3 actions)")),
          hasItemAt(1, equalTo("  [.]1-A passing action-1")),
          hasItemAt(2, equalTo("    [.]0-This passes always-1")),
          hasItemAt(3, equalTo("  [.]3-A passing action-2")),
          hasItemAt(4, equalTo("    [.]2-This passes always-2")),
          hasItemAt(5, equalTo("  [.]5-A passing action-3")),
          hasItemAt(6, equalTo("    [.]4-This passes always-3"))
      ));
      assertThat(getWriter(), hasSize(7));
    }
  }

  public static class CompatForEachAction extends Base {
    @Test
    public void givenPassingConcurrentAction$whenPerformed$thenWorksFine() {
      ////
      // Given concurrent action
      Action action = forEach(
          "i",
          () -> Stream.of("ItemA", "ItemB", "ItemC")
      ).perform(
          sequential(
              simple("Sink-1", (c) -> {
              }),
              simple("Sink-2", (c) -> {
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
              hasItemAt(0, startsWith("[.]0-CompatForEach (SEQUENTIALLY)")),
              hasItemAt(1, equalTo("  [...]2-Sequential (2 actions)")),
              hasItemAt(2, equalTo("    [...]0-Sink-1")),
              hasItemAt(3, equalTo("    [...]1-Sink-2"))
          ));
      assertThat(getWriter(), hasSize(4));
    }

    @Test(expected = RuntimeException.class)
    public void givenFailingConcurrentAction$whenPerformed$thenWorksFine() {
      ////
      // Given concurrent action
      Action action = forEach(
          "i",
          () -> Stream.of("ItemA", "ItemB", "ItemC")
      ).perform(
          sequential(
              simple("Sink-1", (c) -> {
                throw new RuntimeException("Failing");
              }),
              simple("Sink-2", (c) -> {
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
            hasItemAt(0, startsWith("[E]0-CompatForEach")),
            hasItemAt(1, startsWith("  [E]2-Sequential (2 actions)")),
            hasItemAt(2, startsWith("    [E]0-Sink-1")),
            hasItemAt(3, startsWith("    []1-Sink-2"))
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
          nop()
      ).ensure(
          nop()
      );
      performAndPrintAction(action);
      assertThat(getWriter(), allOf(
          hasItemAt(0, equalTo("[.]1-Attempt")),
          hasItemAt(1, equalTo("  [.]0-Target")),
          hasItemAt(2, equalTo("    [.]0-(nop)")),
          hasItemAt(3, equalTo("  []1-Recover(Exception)")),
          hasItemAt(4, equalTo("    []0-(nop)")),
          hasItemAt(5, equalTo("  [.]2-Ensure")),
          hasItemAt(6, equalTo("    [.]0-(nop)"))
      ));
      assertThat(getWriter(), hasSize(7));
    }

    @Test
    public void givenFailingAttemptAction$whenPerformed$thenWorksFine() {
      Action action = attempt(
          simple("Howdy, NPE", new ContextConsumer() {
            @Override
            public void accept(Context context) {
              throw new NullPointerException(this.toString());
            }
          })
      ).recover(
          NullPointerException.class,
          nop()
      ).ensure(
          nop()
      );
      performAndPrintAction(action);
      assertThat(getWriter(), Matchers.allOf(
          hasItemAt(0, equalTo("[.]1-Attempt")),
          hasItemAt(1, equalTo("  [E]0-Target")),
          hasItemAt(2, equalTo("    [E]0-Howdy, NPE")),
          hasItemAt(3, equalTo("  [.]1-Recover(NullPointerException)")),
          hasItemAt(4, equalTo("    [.]0-(nop)")),
          hasItemAt(5, equalTo("  [.]2-Ensure")),
          hasItemAt(6, equalTo("    [.]0-(nop)"))
      ));
      assertThat(getWriter(), hasSize(7));
    }
  }
}
