package com.github.dakusui.actionunit.extras.cmdaction;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import org.junit.Test;

import static com.github.dakusui.actionunit.extras.cmd.Commander.commander;
import static java.util.Arrays.asList;

public class CommanderSupportTest {
  Context context = new Context.Impl();

  @Test
  public void test() {
    perform(
        commander(context, "echo hello").build()
    );
  }

  @Test
  public void test2() {
    perform(
        context.forEachOf(
            asList(1, 2, 3)
        ).perform(
            ($, data) -> commander($, "echo hello:").add(() -> data.get().toString()).build()
        )
    );
  }


  private void perform(Action action) {
    CommanderTestUtil.perform(action);
  }
}
