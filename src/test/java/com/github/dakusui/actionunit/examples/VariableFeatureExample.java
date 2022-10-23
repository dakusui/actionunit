package com.github.dakusui.actionunit.examples;

import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.ut.utils.TestUtils;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import org.junit.Test;

import java.util.stream.Stream;

import static com.github.dakusui.actionunit.core.ActionSupport.*;
import static com.github.dakusui.actionunit.ut.actions.TestFunctionals.constant;
import static com.github.dakusui.actionunit.ut.actions.TestFunctionals.increment;

public class VariableFeatureExample extends TestUtils.TestBase {
  @Test
  public void example() {
    run(
        sequential(
            simple("init", (c) -> c.assignTo("x", 0)),
            compatForEach("i", (c) -> Stream.of("a", "b", "c", "d", "e", "f"))
                .perform(leaf(
                    (Context $) ->
                        sequential(
                            simple(
                                "print i",
                                ($$) -> System.out.printf("%d %s%n", $.<Integer>valueOf("x"), $.valueOf("i"))
                            ),
                            simple(
                                "i++",
                                ($$) -> $.assignTo("i", $.<Integer>valueOf("x") + 1)
                            ))))));
  }

  @Test
  public void example2() {
    run(
        with("x", constant(0)).perform(
            b -> forEach("i", (c) -> Stream.of("a", "b", "c", "d", "e", "f")).perform(
                bb -> sequential(
                    simple(
                        "print i",
                        (cc) -> System.out.printf("%d %s%n", b.contextVariable(cc), bb.contextVariable(cc))),
                    b.updateContextVariableWith(increment())))))
    ;
  }

  private void run(Action action) {
    ReportingActionPerformer performer = ReportingActionPerformer.create();
    performer.performAndReport(action, Writer.Std.OUT);
  }
}
