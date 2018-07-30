package com.github.dakusui.actionunit.extras.cmdaction;

import org.junit.Test;

import java.util.stream.Stream;

import static com.github.dakusui.actionunit.extras.cmdaction.CommanderTestUtil.performAndReport;
import static com.github.dakusui.actionunit.n.core.ActionSupport.cmd;
import static com.github.dakusui.actionunit.n.core.ActionSupport.forEach;

public class CmdExample {

  @Test
  public void example() {
    performAndReport(
        forEach(
            "i",
            () -> Stream.of("a", "b", "c")
        ).perform(
            cmd("echo").addq("hi:").addq(c -> {
              return "hi"; // TODO
            }).addq(":").build()
        )
    );
  }
}
