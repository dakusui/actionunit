package com.github.dakusui.actionunit.extras.cmd;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.stream.Stream;

import static com.github.dakusui.actionunit.core.ActionSupport.cmd;
import static com.github.dakusui.actionunit.core.ActionSupport.forEach;
import static com.github.dakusui.actionunit.extras.cmd.CommanderTestUtil.performAndReport;

public class CmdExample {

  @Test
  public void example() {
    performAndReport(
        forEach(
            "i",
            () -> Stream.of("a", "b", "c")
        ).sequentially(
        ).perform(
            cmd("echo")
                .disconnectStdin()
                .addq("'hi:'")
                .addq(context -> "hi").addq("':'").build()
        )
    );
  }

  @Test
  public void testCmd() {
    performAndReport(
        cmd("echo hello world").build()
    );
  }
}
