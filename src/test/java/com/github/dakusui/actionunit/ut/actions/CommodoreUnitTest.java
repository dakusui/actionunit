package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.actions.cmd.Commodore;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.context.ContextConsumer;
import com.github.dakusui.actionunit.core.context.StreamGenerator;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import com.github.dakusui.cmd.core.process.ProcessStreamer;
import com.github.dakusui.cmd.core.process.Shell;
import org.junit.Test;

import java.util.stream.Stream;

import static com.github.dakusui.actionunit.core.ActionSupport.forEach;
import static com.github.dakusui.actionunit.core.ActionSupport.leaf;
import static com.github.dakusui.actionunit.core.ActionSupport.simple;
import static com.github.dakusui.actionunit.core.ActionSupport.when;
import static com.github.dakusui.cmd.core.process.ProcessStreamer.Checker.createCheckerForExitCode;
import static com.github.dakusui.printables.Printables.isEqualTo;
import static java.util.Arrays.asList;

public class CommodoreUnitTest {
  @Test(expected = ProcessStreamer.Failure.class)
  public void test1() {
    ReportingActionPerformer.create(Writer.Std.OUT).performAndReport(
        localCommander()
            .command("echo hello")
            .toActionWith(createCheckerForExitCode(1))
    );
  }

  @Test
  public void test2() {
    ReportingActionPerformer.create(Writer.Std.OUT).performAndReport(
        localCommander()
            .command(() -> "echo hello")
            .toAction()
    );
  }

  @Test
  public void test3() {
    ReportingActionPerformer.create(Writer.Std.OUT).performAndReport(
        forEach("i",
            localCommander().command(() -> "echo \"Hello World\"").toStreamGenerator())
            .perform(leaf(c -> System.out.println(c.<String>valueOf("i"))))
    );
  }

  @Test(expected = ProcessStreamer.Failure.class)
  public void test4() {
    try {
      ReportingActionPerformer.create(Writer.Std.OUT).performAndReport(
          forEach("i",
              localCommander().command(() -> "echo \"Hello World\"").toStreamGeneratorWith(ProcessStreamer.Checker.createCheckerForExitCode(1)))
              .perform(leaf(c -> System.out.println(c.<String>valueOf("i"))))
      );
    } catch (RuntimeException e) {
      e.printStackTrace();
      throw e;
    }
  }

  @Test
  public void test5() {
    ReportingActionPerformer.create(Writer.Std.OUT).performAndReport(
        when(localCommander().command(() -> "echo \"Hello World\"").toContextPredicate())
            .perform(leaf(ContextConsumer.from(() -> System.out.println("bye"))))
            .otherwise(leaf(c -> System.out.println("Not met")))
    );
  }

  @Test
  public void test6() {
    ReportingActionPerformer.create(Writer.Std.OUT).performAndReport(
        when(localCommander().command(() -> "echo \"Hello World\"").toContextPredicateWith(isEqualTo(1)))
            .perform(leaf(ContextConsumer.from(() -> System.out.println("bye"))))
            .otherwise(leaf(c -> System.out.println("Not met")))
    );
  }

  @Test
  public void test7() {
    ReportingActionPerformer.create(Writer.Std.OUT).performAndReport(
        simple("try simple",
            localCommander().command(() -> "echo hello").toContextConsumerWith(createCheckerForExitCode(0))
        ));
  }

  @Test(expected = ProcessStreamer.Failure.class)
  public void test8() {
    ReportingActionPerformer.create(Writer.Std.OUT).performAndReport(
        simple("try simple",
            localCommander().command(() -> "echo hello").toContextConsumerWith(createCheckerForExitCode(1))
        ));
  }

  @Test
  public void test9() {
    ReportingActionPerformer.create(Writer.Std.OUT).performAndReport(
        simple("try simple",
            localCommander().command(() -> "echo hello").toContextConsumer()
        ));
  }

  @Test
  public void test10() {
    ReportingActionPerformer.create(Writer.Std.OUT).performAndReport(
        forEach("i", StreamGenerator.fromCollection(asList("A", "B", "C")))
            .perform(localCommander().command("echo hello {0}", "i").toAction())
    );
  }

  @Test
  public void test11() {
    ReportingActionPerformer.create(Writer.Std.OUT).performAndReport(
        forEach("i", StreamGenerator.fromArray("A", "B", "C"))
            .perform(localCommander().command(() -> "echo hello {0}", "i").toAction())
    );
  }

  @Test
  public void test12() {
    localCommander()
        .stdin(Stream.of("a", "b", "c"))
        .command(() -> "cat -n")
        .toStreamGenerator().apply(Context.create()).forEach(System.out::println);
  }

  private Commodore localCommander() {
    return new Commodore(Shell.local());
  }

}
