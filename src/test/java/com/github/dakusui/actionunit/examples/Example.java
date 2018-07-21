package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.ut.utils.TestUtils;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import org.junit.Test;

import java.util.stream.Stream;

import static com.github.dakusui.actionunit.core.ActionSupport.*;

public class Example extends TestUtils.TestBase {
  @Test
  public void test() {
    Action action = forEach(
        "i",
        () -> Stream.of("hello", "world")
    ).parallelly(
    ).perform(
        sequential(
            simple("print", (c) -> System.out.println(v(c))),
            simple("print", (c) -> System.out.println(v(c))),
            sequential(
                simple("print", (c) -> System.out.println(v(c))),
                simple("print", (c) -> System.out.println(v(c)))
            )
        )
    );

    ReportingActionPerformer.create(Writer.Std.OUT).performAndReport(action);
  }

  static private String v(Context c) {
    return c.valueOf("v");
  }
}
