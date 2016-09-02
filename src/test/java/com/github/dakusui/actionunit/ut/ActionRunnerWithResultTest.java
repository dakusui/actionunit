package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.actionunit.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import org.junit.Test;

import static com.github.dakusui.actionunit.Actions.simple;
import static com.github.dakusui.actionunit.utils.TestUtils.hasItemAt;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertTrue;

public class ActionRunnerWithResultTest {
  final TestUtils.Out           out    = new TestUtils.Out();
  final ActionRunner.WithResult runner = new ActionRunner.WithResult();

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
    //When printed (without being run)
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
    //When printed (without being run)
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
    //When printed (without being run)
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

  public Action createPassingAction() {
    return simple("A passing action", new Runnable() {
      @Override
      public void run() {
      }

      @Override
      public String toString() {
        return "This passes always";
      }
    });
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
