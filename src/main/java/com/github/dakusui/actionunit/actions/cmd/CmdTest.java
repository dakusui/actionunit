package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.actionunit.core.ActionSupport;
import com.github.dakusui.actionunit.core.context.ContextConsumer;
import com.github.dakusui.actionunit.core.context.StreamGenerator;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import org.junit.Test;

import static com.github.dakusui.actionunit.core.ActionSupport.*;

public class CmdTest {
  @Test
  public void test() {
    ReportingActionPerformer.create().performAndReport(
        cmd("echo hello").downstreamConsumer(System.out::println).toAction(), Writer.Std.OUT
    );
  }

  @Test
  public void test1_1() {
    ReportingActionPerformer.create().performAndReport(
        forEach("i",
            cmd("echo hello && echo world").toStreamGenerator()
        ).perform(
            leaf(ContextConsumer.of(
                () -> "print 'i'",
                context -> System.out.println("i=" + context.valueOf("i"))))),
        Writer.Std.OUT
    );
  }

  @Test
  public void test2() {
    ReportingActionPerformer.create().performAndReport(
        ActionSupport.forEach("i", StreamGenerator.fromArray("hello", "world")).perform(
            cmd("echo {{0}}", "i").downstreamConsumer(System.out::println).toAction()
        ),
        Writer.Std.OUT
    );
  }

  @Test
  public void test3() {
    ReportingActionPerformer.create().performAndReport(
        ActionSupport.forEach("i", StreamGenerator.fromArray("hello", "world")).perform(
            cmd("echo {{0}}", "i").downstreamConsumer(System.out::println).toAction()
        ),
        Writer.Std.OUT
    );
  }
}
