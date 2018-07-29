package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.compat.utils.TestUtils;
import com.github.dakusui.actionunit.n.core.Action;
import com.github.dakusui.actionunit.n.core.Context;
import com.github.dakusui.actionunit.n.io.Writer;
import com.github.dakusui.actionunit.n.visitors.ReportingActionPerformer;
import org.junit.Test;

import java.util.stream.Stream;

import static com.github.dakusui.actionunit.n.core.ActionSupport.*;

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
