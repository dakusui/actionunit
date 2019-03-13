package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.actions.cmd.BaseCommander;
import com.github.dakusui.actionunit.actions.cmd.CommandLineComposer;
import com.github.dakusui.actionunit.actions.cmd.Commander;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.context.StreamGenerator;
import com.github.dakusui.actionunit.core.context.multiparams.MultiParameters;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import com.github.dakusui.processstreamer.core.process.ProcessStreamer;
import com.github.dakusui.processstreamer.core.process.Shell;
import org.junit.Test;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static com.github.dakusui.actionunit.core.ActionSupport.forEach;
import static com.github.dakusui.actionunit.core.ActionSupport.leaf;
import static com.github.dakusui.actionunit.core.ActionSupport.simple;
import static com.github.dakusui.actionunit.core.ActionSupport.when;
import static com.github.dakusui.actionunit.core.context.ContextFunctions.contextConsumerFor;
import static com.github.dakusui.crest.Crest.allOf;
import static com.github.dakusui.crest.Crest.asInteger;
import static com.github.dakusui.crest.Crest.asString;
import static com.github.dakusui.crest.Crest.assertThat;
import static com.github.dakusui.printables.Printables.isEqualTo;
import static com.github.dakusui.processstreamer.core.process.ProcessStreamer.Checker.createCheckerForExitCode;
import static java.util.Arrays.asList;

public class CommanderUnitTest {
  @Test(expected = ProcessStreamer.Failure.class)
  public void test1() {
    ReportingActionPerformer.create().performAndReport(
        localCommander()
            .command("echo hello")
            .toActionWith(createCheckerForExitCode(1)),
        Writer.Std.OUT
    );
  }

  @Test
  public void test2() {
    ReportingActionPerformer.create().performAndReport(
        localCommander()
            .command(() -> "echo hello")
            .toAction(),
        Writer.Std.OUT
    );
  }

  @Test
  public void test3() {
    ReportingActionPerformer.create().performAndReport(
        forEach("i",
            localCommander().command(() -> "echo \"Hello World\"").toStreamGenerator())
            .perform(leaf(c -> System.out.println(c.<String>valueOf("i")))),
        Writer.Std.OUT
    );
  }

  @Test(expected = ProcessStreamer.Failure.class)
  public void test4() {
    try {
      ReportingActionPerformer.create().performAndReport(
          forEach("i",
              localCommander().command(() -> "echo \"Hello World\"").toStreamGeneratorWith(ProcessStreamer.Checker.createCheckerForExitCode(1)))
              .perform(leaf(c -> System.out.println(c.<String>valueOf("i")))),
          Writer.Std.OUT
      );
    } catch (RuntimeException e) {
      e.printStackTrace();
      throw e;
    }
  }

  @Test
  public void test5() {
    ReportingActionPerformer.create().performAndReport(
        when(localCommander().command(() -> "echo \"Hello World\"").toContextPredicate())
            .perform(leaf(MultiParameters.Consumers.from(() -> System.out.println("bye"))))
            .otherwise(leaf(c -> System.out.println("Not met"))),
        Writer.Std.OUT
    );
  }

  @Test
  public void test6() {
    ReportingActionPerformer.create().performAndReport(
        when(localCommander().command(() -> "echo \"Hello World\"").toContextPredicateWith(isEqualTo(1)))
            .perform(leaf(MultiParameters.Consumers.from(() -> System.out.println("bye"))))
            .otherwise(leaf(c -> System.out.println("Not met"))),
        Writer.Std.OUT
    );
  }

  @Test
  public void test7() {
    ReportingActionPerformer.create().performAndReport(
        simple("try simple",
            localCommander().command(() -> "echo hello").toContextConsumerWith(createCheckerForExitCode(0))
        ),
        Writer.Std.OUT);
  }

  @Test(expected = ProcessStreamer.Failure.class)
  public void test8() {
    ReportingActionPerformer.create().performAndReport(
        simple("try simple",
            localCommander().command(() -> "echo hello").toContextConsumerWith(createCheckerForExitCode(1))
        ),
        Writer.Std.OUT);
  }

  @Test
  public void test9() {
    ReportingActionPerformer.create().performAndReport(
        simple("try simple",
            localCommander().command(() -> "echo hello").toContextConsumer()
        ),
        Writer.Std.OUT);
  }

  @Test
  public void test10() {
    ReportingActionPerformer.create().performAndReport(
        forEach("i", StreamGenerator.fromCollection(asList("A", "B", "C")))
            .perform(localCommander().command("echo hello {0}", "i").toAction()),
        Writer.Std.OUT
    );
  }

  @Test
  public void test11() {
    ReportingActionPerformer.create().performAndReport(
        forEach("i", StreamGenerator.fromArray("A", "B", "C"))
            .perform(localCommander().command(() -> "echo hello {0}", "i").toAction()),
        Writer.Std.OUT
    );
  }

  @Test
  public void test11b() {
    ReportingActionPerformer.create().performAndReport(
        forEach("i", StreamGenerator.fromArray("A", "B", "C"))
            .perform(localCommander().command(
                CommandLineComposer.create("echo hello {{i}}", "i"),
                "i").toAction()),
        Writer.Std.OUT
    );
  }


  @Test
  public void test12() {
    localCommander()
        .stdin(Stream.of("a", "b", "c"))
        .command(() -> "cat -n")
        .toStreamGenerator().apply(Context.create()).forEach(System.out::println);
  }

  @Test
  public void test13() {
    List<String> out = new LinkedList<>();
    ReportingActionPerformer.create().performAndReport(
        forEach("i", localCommander()
            .cwd(new File(System.getProperty("user.home")))
            .command(CommandLineComposer.create("pwd")).toStreamGenerator())
            .perform(
                leaf(context -> out.add(context.valueOf("i")))),
        Writer.Std.OUT);

    assertThat(
        out,
        allOf(
            asInteger("size").equalTo(1).$(),
            asString("get", 0).equalTo(System.getProperty("user.home")).$())
    );
  }

  @Test
  public void test14() {
    List<String> out = new LinkedList<>();
    ReportingActionPerformer.create().performAndReport(
        forEach("i", localCommander()
            .env("hello", "world")
            .command(CommandLineComposer.create("echo ${hello}")).toStreamGenerator())
            .perform(
                leaf(context -> out.add(context.valueOf("i")))),
        Writer.Std.OUT);

    assertThat(
        out,
        allOf(
            asInteger("size").equalTo(1).$(),
            asString("get", 0).equalTo("world").$())
    );
  }

  @Test
  public void test15() {
    List<String> out = new LinkedList<>();
    ReportingActionPerformer.create().performAndReport(
        forEach("i", localCommander()
            .env("hello", "world")
            .stdoutConsumer(out::add)
            .command(CommandLineComposer.create("echo ${hello}")).toStreamGenerator())
            .perform(
                leaf(contextConsumerFor("i").with(context -> out.add(context.valueOf("i"))))),
        Writer.Std.OUT);

    assertThat(
        out,
        allOf(
            asInteger("size").equalTo(2).$(),
            asString("get", 0).equalTo("world").$(),
            asString("get", 1).equalTo("world").$())
    );
  }

  private Commander localCommander() {
    return new BaseCommander(Shell.local());
  }

}
