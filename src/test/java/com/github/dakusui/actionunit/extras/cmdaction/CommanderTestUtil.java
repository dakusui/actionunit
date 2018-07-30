package com.github.dakusui.actionunit.extras.cmdaction;

import com.github.dakusui.actionunit.n.core.Action;
import com.github.dakusui.actionunit.n.io.Writer;
import com.github.dakusui.actionunit.n.visitors.ReportingActionPerformer;

enum CommanderTestUtil {
  ;

  static void performAndReport(Action action) {
    ReportingActionPerformer.create(Writer.Std.OUT).performAndReport(action);
  }
}
