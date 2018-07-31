package com.github.dakusui.actionunit.extras.cmd;

import org.junit.Test;

import java.util.stream.Stream;

import static com.github.dakusui.actionunit.extras.cmd.CommanderTestUtil.performAndReport;
import static com.github.dakusui.actionunit.core.ActionSupport.cmd;
import static com.github.dakusui.actionunit.core.ActionSupport.forEach;

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
