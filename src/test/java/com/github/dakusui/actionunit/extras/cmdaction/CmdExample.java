package com.github.dakusui.actionunit.extras.cmdaction;

import com.github.dakusui.actionunit.core.Context;
import org.junit.Test;

import static com.github.dakusui.actionunit.extras.cmdaction.CommanderTestUtil.perform;

public class CmdExample {
  private Context context = new Context.Impl();

  @Test
  public void example() {
    perform(
        context.forEachOf(
            "a", "b", "c"
        ).perform(
            ($, data) -> $.cmd("echo").addq("hi:").addq(data).addq(":").build()
        )
    );
  }
}
