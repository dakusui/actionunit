package com.github.dakusui.actionunit.extras.cmdaction;

import com.github.dakusui.actionunit.n.core.Action;
import com.github.dakusui.actionunit.n.visitors.ReportingActionPerformer;

enum CommanderTestUtil {
  ;

  static void perform(Action action) {
    ReportingActionPerformer performer = ReportingActionPerformer.create();
    action.accept(performer);
    // TODO
    //    performer.report();
    //    try {
    //      performer.perform();
    //    } finally {
    //      performer.report();
    //    }
  }
}
