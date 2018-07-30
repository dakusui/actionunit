package com.github.dakusui.actionunit.extras.cmdaction;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;

enum CommanderTestUtil {
  ;

  static void performAndReport(Action action) {
    ReportingActionPerformer.create(Writer.Std.OUT).performAndReport(action);
  }
}
