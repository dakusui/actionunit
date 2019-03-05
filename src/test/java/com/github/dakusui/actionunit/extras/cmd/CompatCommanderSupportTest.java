package com.github.dakusui.actionunit.extras.cmd;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.ut.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import com.github.dakusui.processstreamer.core.process.ProcessStreamer;
import org.junit.Test;

import java.util.stream.Stream;

import static com.github.dakusui.actionunit.actions.cmd.compat.CompatCommander.commander;
import static com.github.dakusui.actionunit.core.ActionSupport.cmd;
import static com.github.dakusui.actionunit.core.ActionSupport.forEach;
import static com.github.dakusui.actionunit.core.ActionSupport.leaf;

public class CompatCommanderSupportTest extends TestUtils.TestBase {

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

  @Test(expected = ProcessStreamer.Failure.class)
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
    ReportingActionPerformer performer = ReportingActionPerformer.create();
    action.accept(performer);
  }
}
