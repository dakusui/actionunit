package com.github.dakusui.actionunit.extras.cmdaction;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.visitors.reporting.ReportingActionPerformer;

enum CommanderTestUtil {
  ;

  static void perform(Action action) {
    ReportingActionPerformer performer = ReportingActionPerformer.create(action);
    performer.report();
    try {
      performer.perform();
    } finally {
      performer.report();
    }
  }
}
