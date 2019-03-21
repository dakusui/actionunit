package com.github.dakusui.actionunit.ut.actions.cmd;

import com.github.dakusui.actionunit.actions.cmd.Cmd;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.context.ContextConsumer;
import com.github.dakusui.actionunit.core.context.StreamGenerator;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import com.github.dakusui.processstreamer.core.process.ProcessStreamer;
import org.junit.Test;

import static com.github.dakusui.actionunit.core.ActionSupport.cmd;
import static com.github.dakusui.actionunit.core.ActionSupport.forEach;
import static com.github.dakusui.actionunit.core.ActionSupport.leaf;
import static com.github.dakusui.crest.Crest.asString;
import static com.github.dakusui.crest.Crest.assertThat;

public class CmdTest {
  @Test
  public void test() {
    performAction(initCmd(cmd("echo hello")).toAction());
  }

  @Test
  public void test1_1() {
    performAction(
        forEach("i",
            cmd("echo hello && echo world").toStreamGenerator()
        ).perform(
            leaf(ContextConsumer.of(
                () -> "print 'i'",
                context -> System.out.println("i=" + context.valueOf("i")))))
    );
  }

  @Test
  public void test2() {
    performAsActionInsideLoop(cmd("echo {{0}}", "i"));
  }

  @Test
  public void test2c() {
    performAsContextConsumerInsideLoop(cmd("echo {{0}}", "i"));
  }

  @Test
  public void test3() {
    performAsActionInsideLoop(cmd("echo {{0}}", "i"));
  }

  @Test
  public void test4() {
    performAsActionInsideLoop(cmd("echo", "i").append(" ").appendVariable("i"));
  }

  @Test(expected = ProcessStreamer.Failure.class)
  public void test4E() {
    try {
      performAsActionInsideLoop(cmd("UNKNOWN_COMMAND", "i").append(" ").appendVariable("i"));
    } catch (ProcessStreamer.Failure failure) {
      assertThat(
          failure.getMessage(),
          asString().containsString("UNKNOWN_COMMAND hello").$()
      );
      throw failure;
    }
  }

  private static void performAsContextConsumerInsideLoop(Cmd cmd) {
    performAction(
        forEach("i", StreamGenerator.fromArray("hello", "world")).perform(
            leaf(initCmd(cmd).toContextConsumer())
        ));
  }


  private static void performAsActionInsideLoop(Cmd cmd) {
    performAction(
        forEach("i", StreamGenerator.fromArray("hello", "world")).perform(
            initCmd(cmd).toAction()
        ));
  }

  private static Cmd initCmd(Cmd cmd) {
    return cmd.downstreamConsumer(System.out::println);
  }

  private static void performAction(Action action) {
    ReportingActionPerformer.create().performAndReport(
        action,
        Writer.Std.OUT
    );
  }
}
