package com.github.dakusui.actionunit.scenarios;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.ActionSupport;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import org.junit.Test;


public class EnsuredTest {

  @Test
  public void givenEnsure_whenPerform_thenBehavesAsExpected() {
    Action action = ActionSupport.ensure(passesOnAttempt(2))
        .recoverExceptions(e -> e instanceof IntentionalFailure)
        .withNop()
        .withNop()
        .withNop()
        .withNop()
        .build();

    run(action);


  }

  /**
   * Returns an action which passes at the ith attempt.
   *
   * @param i An index for attempt. Begins with 0.
   * @return An action which passes at the ith attempt.
   */
  private static Action passesOnAttempt(int i) {
    return ActionSupport.named("passesOn[" + i + "]", ActionSupport.leaf(c -> {
      int times = 0;
      if (!c.defined("times") || (times = c.valueOf("times")) < i) {
        c.assignTo("times", ++times);
        throw new IntentionalFailure(times);
      }
      System.out.println("hello");
    }));
  }

  private static void run(Action action) {
    ReportingActionPerformer.create().performAndReport(action, Writer.Std.OUT);
  }

  static class IntentionalFailure extends RuntimeException {
    public IntentionalFailure(int i) {
      super(i + "th try");
    }
  }
}
