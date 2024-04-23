package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.ut.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import org.junit.Ignore;
import org.junit.Test;

import java.util.stream.Stream;

import static com.github.dakusui.actionunit.core.ActionSupport.*;
import static com.github.dakusui.actionunit.ut.actions.TestFunctionals.constant;
import static com.github.dakusui.actionunit.ut.actions.TestFunctionals.printVariable;

public class Example extends TestUtils.TestBase {
  @Ignore
  @Test
  public void test() {
    Action action = forEach(
        constant(Stream.of("hello", "world"))).action(
        sequential(
            simple("print1", printVariable()),
            simple("print2", printVariable()),
            sequential(
                simple("print2-1", printVariable()),
                simple("print2-2", printVariable())))).$();

    ReportingActionPerformer.create().performAndReport(action, Writer.Std.OUT);
  }

  @Ignore
  @Test
  public void test2() {
    Action action = sequential(
        sequential(
            sequential(
                simple("print2-1", printVariable()),
                simple("print2-2", printVariable()))));

    ReportingActionPerformer.create().performAndReport(action, Writer.Std.OUT);
  }

  static private String v(Context c) {
    return c.valueOf("i");
  }
}
