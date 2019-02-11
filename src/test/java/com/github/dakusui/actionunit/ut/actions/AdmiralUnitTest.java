package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.actions.cmd.Admiral;
import com.github.dakusui.actionunit.core.ContextConsumer;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import com.github.dakusui.cmd.core.process.ProcessStreamer;
import com.github.dakusui.cmd.core.process.Shell;
import org.junit.Test;

import static com.github.dakusui.actionunit.core.ActionSupport.forEach;
import static com.github.dakusui.actionunit.core.ActionSupport.leaf;
import static com.github.dakusui.actionunit.core.ActionSupport.simple;
import static com.github.dakusui.actionunit.core.ActionSupport.when;
import static com.github.dakusui.cmd.core.process.ProcessStreamer.Checker.createCheckerForExitCode;
import static com.github.dakusui.printables.Printables.equalsTo;

public class AdmiralUnitTest {
  @Test(expected = ProcessStreamer.Failure.class)
  public void test1() {
    ReportingActionPerformer.create(Writer.Std.OUT).performAndReport(
        new Admiral(Shell.local()).toActionWith(() -> "echo hello", createCheckerForExitCode(1))
    );
  }

  @Test
  public void test2() {
    ReportingActionPerformer.create(Writer.Std.OUT).performAndReport(
        new Admiral(Shell.local()).toActionWith(() -> "echo hello")
    );
  }

  @Test
  public void test3() {
    ReportingActionPerformer.create(Writer.Std.OUT).performAndReport(
        forEach("i", new Admiral(Shell.local()).toStreamGenerator(() -> "echo \"Hello World\""))
            .perform(leaf(c -> System.out.println(c.<String>valueOf("i"))))
    );
  }

  @Test(expected = ProcessStreamer.Failure.class)
  public void test4() {
    ReportingActionPerformer.create(Writer.Std.OUT).performAndReport(
        forEach("i",
            new Admiral(Shell.local()).toStreamGenerator(() -> "echo \"Hello World\"", ProcessStreamer.Checker.createCheckerForExitCode(1)))
            .perform(leaf(c -> System.out.println(c.<String>valueOf("i"))))
    );
  }

  @Test
  public void test5() {
    ReportingActionPerformer.create(Writer.Std.OUT).performAndReport(
        when(new Admiral(Shell.local()).toContextPredicate(() -> "echo \"Hello World\"", equalsTo(0)))
            .perform(leaf(ContextConsumer.from(() -> System.out.println("bye"))))
            .otherwise(leaf(c -> System.out.println("Not met")))
    );
  }

  @Test
  public void test6() {
    ReportingActionPerformer.create(Writer.Std.OUT).performAndReport(
        when(new Admiral(Shell.local()).toContextPredicate(() -> "echo \"Hello World\"", equalsTo(1)))
            .perform(leaf(ContextConsumer.from(() -> System.out.println("bye"))))
            .otherwise(leaf(c -> System.out.println("Not met")))
    );
  }

  @Test
  public void test7() {
    ReportingActionPerformer.create(Writer.Std.OUT).performAndReport(
        simple("try simple",
            new Admiral(Shell.local()).toContextConsumer(() -> "echo hello", createCheckerForExitCode(0))
        ));
  }

  @Test(expected = ProcessStreamer.Failure.class)
  public void test8() {
    ReportingActionPerformer.create(Writer.Std.OUT).performAndReport(
        simple("try simple",
            new Admiral(Shell.local()).toContextConsumer(() -> "echo hello", createCheckerForExitCode(1))
        ));
  }

  @Test
  public void test9() {
    ReportingActionPerformer.create(Writer.Std.OUT).performAndReport(
        simple("try simple",
            new Admiral(Shell.local()).toContextConsumer(() -> "echo hello")
        ));
  }

}
