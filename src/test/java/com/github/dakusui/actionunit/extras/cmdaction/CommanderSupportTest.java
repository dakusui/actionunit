package com.github.dakusui.actionunit.extras.cmdaction;

import com.github.dakusui.actionunit.n.core.Action;
import com.github.dakusui.actionunit.n.visitors.ReportingActionPerformer;
import org.junit.Test;

import java.util.stream.Stream;

import static com.github.dakusui.actionunit.n.actions.cmd.Commander.commander;
import static com.github.dakusui.actionunit.n.core.ActionSupport.*;

public class CommanderSupportTest {

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
    ReportingActionPerformer performer = ReportingActionPerformer.create();
    action.accept(performer);
  }
}
