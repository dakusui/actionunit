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

public class Example extends TestUtils.TestBase {
  @Ignore
  @Test
  public void test() {
    Action action = forEach(
        "i",
        (c) -> Stream.of("hello", "world")
    ).parallelly(
    ).perform(
        sequential(
            simple("print1", (c) -> System.out.println(v(c))),
            simple("print2", (c) -> System.out.println(v(c))),
            sequential(
                simple("print2-1", (c) -> System.out.println(v(c))),
                simple("print2-2", (c) -> System.out.println(v(c)))
            )
        )
    );

    ReportingActionPerformer.create().performAndReport(action, Writer.Std.OUT);
  }

  @Ignore
  @Test
  public void test2() {
    Action action = sequential(
        sequential(
            sequential(
                simple("print2-1", (c) -> System.out.println(v(c))),
                simple("print2-2", (c) -> System.out.println(v(c)))
            )
        )
    );

    ReportingActionPerformer.create().performAndReport(action, Writer.Std.OUT);
  }

  static private String v(Context c) {
    return c.valueOf("v");
  }
}
