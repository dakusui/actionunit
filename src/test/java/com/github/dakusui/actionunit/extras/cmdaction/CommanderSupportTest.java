package com.github.dakusui.actionunit.extras.cmdaction;

import com.github.dakusui.actionunit.compat.utils.TestUtils;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import org.junit.Test;

import java.util.stream.Stream;

import static com.github.dakusui.actionunit.actions.cmd.Commander.commander;
import static com.github.dakusui.actionunit.core.ActionSupport.*;

public class CommanderSupportTest extends TestUtils.TestBase {

  @Test
  public void test() {
    perform(
        cmd("echo hello").build()
    );
  }

  @Test
  public void test2() {
    perform(
        forEach(
            "i",
            () -> Stream.of(1, 2, 3)
        ).perform(
            leaf(context -> commander("echo hello:").add(c -> c.valueOf("i")).build())
        )
    );
  }

  private void perform(Action action) {
    ReportingActionPerformer performer = ReportingActionPerformer.create(Writer.Std.OUT);
    action.accept(performer);
  }
}
