package com.github.dakusui.actionunit.extras.cmd;

import com.github.dakusui.actionunit.ut.utils.TestUtils;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import com.github.dakusui.cmd.exceptions.UnexpectedExitValueException;
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
            (c) -> Stream.of(1, 2, 3)
        ).perform(
            leaf(context -> commander("echo hello:").add(c -> c.valueOf("i")).build())
        )
    );
  }

  @Test(expected = UnexpectedExitValueException.class)
  public void test3() {
    perform(
        forEach(
            "i",
            (c) -> Stream.of(1, 2, 3)
        ).perform(
            leaf(context -> commander("unknwonEcho hello:").add(c -> c.valueOf("i").toString()).toStream(context).forEach(System.out::println))
        )
    );
  }

  private void perform(Action action) {
    ReportingActionPerformer performer = ReportingActionPerformer.create(Writer.Std.OUT);
    action.accept(performer);
  }
}
