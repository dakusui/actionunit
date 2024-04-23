package com.github.dakusui.actionunit.ut.actions.cmd;

import com.github.dakusui.actionunit.core.ActionSupport;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import org.junit.Test;

public class UnixTest {
  @Test
  public void test() {
    ReportingActionPerformer.create().performAndReport(
        ActionSupport.unix().echo().message("hello, world").toAction(),
        Writer.Std.OUT
    );
  }
}
