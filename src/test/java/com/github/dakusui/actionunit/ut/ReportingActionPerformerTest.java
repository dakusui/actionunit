package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.context.ContextConsumer;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.ut.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.ActionPerformer;
import com.github.dakusui.actionunit.visitors.ActionPrinter;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import com.github.dakusui.crest.Crest;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.stream.Stream;

import static com.github.dakusui.actionunit.core.ActionSupport.*;
import static com.github.dakusui.crest.Crest.*;

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
      ReportingActionPerformer.create().performAndReport(action, getWriter());
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
      //noinspection
      assertThat(
          TestUtils.removeSpentTimeFromResultColumn(getWriter()),
          allOf(
              asString("get", 0).equalTo("[o]A passing action").$(),
              asString("get", 1).equalTo("  [o]This passes always:(noname)").$()
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
        //noinspection
        assertThat(
            TestUtils.removeSpentTimeFromResultColumn(getWriter()),
            allOf(
                asString("get", 0).startsWith("[F]A failing action").$(),
                asString("get", 1).equalTo("  [F]This fails always:(noname)").$()
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
        //noinspection
        assertThat(
            TestUtils.removeSpentTimeFromResultColumn(getWriter()),
            allOf(
                asString("get", 0).startsWith("[E]An error action").$(),
                asString("get", 1).equalTo("  [E]This gives a runtime exception always:(noname)").$(),
                asInteger("size").equalTo(2).$()
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
      assertThat(
          TestUtils.removeSpentTimeFromResultColumn(getWriter()),
          allOf(
              asString("get", 0).equalTo("*-[o]A passing action-1").$(),
              asString("get", 1).equalTo("|   [o]This passes always-1:(noname)").$(),
              asString("get", 2).equalTo("*-[o]A passing action-2").$(),
              asString("get", 3).equalTo("|   [o]This passes always-2:(noname)").$(),
              asString("get", 4).equalTo("*-[o]A passing action-3").$(),
              asString("get", 5).equalTo("    [o]This passes always-3:(noname)").$(),
              asInteger("size").equalTo(6).$()
          ));
    }
  }

  public static class CompatForEachAction extends Base {
    @Test
    public void givenPassingConcurrentAction$whenPerformed$thenWorksFine() {
      ////
      // Given concurrent action
      Action action = compatForEach(
          "i",
          (c) -> Stream.of("ItemA", "ItemB", "ItemC")
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
      //noinspection
      assertThat(
          TestUtils.removeSpentTimeFromResultColumn(getWriter()),
          allOf(
              asString("get", 0).startsWith("[o]for each of (noname) sequentially").$(),
              asString("get", 1).equalTo("  +-[ooo]Sink-1:(noname)").$(),
              asString("get", 2).equalTo("  +-[ooo]Sink-2:(noname)").$(),
              asInteger("size").equalTo(3).$()
          ));
    }

    @Test(expected = RuntimeException.class)
    public void givenFailingConcurrentAction$whenPerformed$thenWorksFine() {
      ////
      // Given concurrent action
      Action action = compatForEach(
          "i",
          (c) -> Stream.of("ItemA", "ItemB", "ItemC")
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
        //noinspection
        assertThat(
            TestUtils.removeSpentTimeFromResultColumn(getWriter()),
            allOf(
                asString("get", 0).startsWith("[E]for each of (noname) sequentially").$(),
                asString("get", 1).startsWith("  +-[E]Sink-1:(noname)").$(),
                asString("get", 2).startsWith("  +-[]Sink-2:(noname)").$(),
                asInteger("size").equalTo(3).$()
            ));
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
          asString("get", 0).matchesRegex("\\[o:[0-9]+\\]attempt").$(),
          asString("get", 1).matchesRegex("  \\[o:[0-9]+\\]\\(nop\\)").$(),
          asString("get", 2).equalTo("  []recover").$(),
          asString("get", 3).equalTo("    [](nop)").$(),
          asString("get", 4).matchesRegex("  \\[o:[0-9]+\\]ensure").$(),
          asString("get", 5).matchesRegex("    \\[o:[0-9]+\\]\\(nop\\)").$(),
          asInteger("size").equalTo(6).$()
      ));
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
      assertThat(getWriter(), Crest.allOf(
          asString("get", 0).matchesRegex("\\[o:[0-9]+\\]attempt").$(),
          asString("get", 1).matchesRegex("  \\[E:[0-9]+\\]Howdy, NPE:\\(noname\\)").$(),
          asString("get", 2).matchesRegex("  \\[o:[0-9]+\\]recover").$(),
          asString("get", 3).matchesRegex("    \\[o:[0-9]+\\]\\(nop\\)").$(),
          asString("get", 4).matchesRegex("  \\[o:[0-9]+\\]ensure").$(),
          asString("get", 5).matchesRegex("    \\[o:[0-9]+\\]\\(nop\\)").$(),
          asInteger("size").equalTo(6).$()
      ));
    }
  }
}
