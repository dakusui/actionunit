package com.github.dakusui.actionunit.ut.actions.cmd;

import com.github.dakusui.actionunit.actions.ContextVariable;
import com.github.dakusui.actionunit.actions.RetryOption;
import com.github.dakusui.actionunit.actions.cmd.*;
import com.github.dakusui.actionunit.actions.cmd.unix.Cmd;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.context.ContextConsumer;
import com.github.dakusui.actionunit.core.context.StreamGenerator;
import com.github.dakusui.actionunit.ut.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import com.github.dakusui.crest.utils.printable.Printable;
import com.github.dakusui.processstreamer.core.process.ProcessStreamer;
import com.github.dakusui.processstreamer.core.process.Shell;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.github.dakusui.actionunit.core.ActionSupport.*;
import static com.github.dakusui.actionunit.core.context.ContextFunctions.*;
import static com.github.dakusui.crest.Crest.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

@RunWith(Enclosed.class)
public class CmdTest extends TestUtils.TestBase {
  private static class Base extends TestUtils.TestBase {
    List<String> report = new LinkedList<>();
    List<String> out    = new LinkedList<>();

    void performAction(Action action) {
      ReportingActionPerformer.create().performAndReport(
          action,
          s -> {
            System.out.println(s);
            report.add(s);
          }
      );
    }
  }

  public static class AsCommander extends Base {
    @Test
    public void givenCommanderObject$whenExerciseGetters$thenNoExceptionThrown() {
      Commander<?> commander = cmd("echo")
          .append("${ENVVAR_HELLO}")
          .setenv("ENVVAR_HELLO", "world")
          .downstreamConsumer(toStdoutAndGivenList(out));
      requireThat(commander.retryOption(), asObject().isNotNull().$());
      requireThat(commander.checkerFactory(), asObject().isNotNull().$());
      requireThat(commander.downstreamConsumerFactory(), asObject().isNotNull().$());
      requireThat(commander.stdin(), asObject().isNotNull().$());
      requireThat(commander.shellManager(), asObject().isNotNull().$());
      requireThat(commander.envvars(), asObject().isNotNull().$());
      requireThat(commander.cwd(), asObject().isNotNull().$());
    }

    @Test
    public void whenExtendCommanderOverridingBuildCommandLineComposerMethod$thenCompiles() {
      // This test only makes sure buildCommandLineComposer can be overridden.
      requireThat(
          new Cmd(CommanderConfig.DEFAULT) {
            public CommandLineComposer buildCommandLineComposer() {
              return super.buildCommandLineComposer();
            }
          },
          asObject().isNotNull().$());
    }

    @Test
    public void givenEchoEnvVar$whenPerformAsAction$thenEnvVarValuePrinted() {
      performAction(
          cmd("echo")
              .append("${ENVVAR_HELLO}")
              .setenv("ENVVAR_HELLO", "world")
              .downstreamConsumer(toStdoutAndGivenList(out))
              .toAction());
      assertThat(
          out,
          asListOf(String.class).equalTo(singletonList("world")).$()
      );
    }

    @Test
    public void givenEchoHelloWithSh$whenPerformAsAction$thenPrinted() {
      performAction(
          cmd("echo")
              .append("hello")
              .shellManager(ShellManager.createShellManager())
              .downstreamConsumer(toStdoutAndGivenList(out))
              .toAction());
      assertThat(
          out,
          asListOf(String.class).equalTo(singletonList("hello")).$()
      );
    }

    @Test
    public void givenEchoHelloUsingAddMethod$whenPerformAsAction$thenPrinted() {
      performAction(
          cmd("echo")
              .add("hello")
              .downstreamConsumer(toStdoutAndGivenList(out))
              .toAction());
      assertThat(
          out,
          asListOf(String.class).equalTo(singletonList("hello")).$()
      );
    }

    @Test
    public void givenEchoHelloUsingAppendqMethod$whenPerformAsAction$thenPrinted() {
      performAction(
          cmd("echo")
              .appendq(immediateOf("hello"))
              .downstreamConsumer(toStdoutAndGivenList(out))
              .toAction());
      assertThat(
          out,
          asListOf(String.class).equalTo(singletonList("hello")).$()
      );
    }

    @Test
    public void givenEchoHelloUsingAppendContextFunctionMethod$whenPerformAsAction$thenPrinted() {
      performAction(
          cmd("echo")
              .append(immediateOf("hello"))
              .downstreamConsumer(toStdoutAndGivenList(out))
              .toAction());
      assertThat(
          out,
          asListOf(String.class).equalTo(singletonList("hello")).$()
      );
    }

    @Test
    public void givenEchoHelloUsingAppendContextFunctionMethod2$whenPerformAsAction$thenPrinted() {
      performAction(
          cmd("echo")
              .append(immediateOf("hello"), false)
              .downstreamConsumer(toStdoutAndGivenList(out))
              .toAction());
      assertThat(
          out,
          asListOf(String.class).equalTo(singletonList("hello")).$()
      );
    }

    @Test
    public void givenEchoHelloUsingAppendStringMethod$whenPerformAsAction$thenPrinted() {
      performAction(
          cmd("echo")
              .append("hello", false)
              .downstreamConsumer(toStdoutAndGivenList(out))
              .toAction());
      assertThat(
          out,
          asListOf(String.class).equalTo(singletonList("hello")).$()
      );
    }

    @Test
    public void givenEchoHelloUsingAppendVariableMethod$whenPerformAsAction$thenPrinted() {
      performAction(
          forEach("i", StreamGenerator.fromArray("hello", "world"))
              .perform(i -> cmd("echo")
                  .appendVariable(i, false)
                  .downstreamConsumer(toStdoutAndGivenList(out)).$()));
      assertThat(
          out,
          asListOf(String.class).equalTo(asList("hello", "world")).$()
      );
    }

    @Test
    public void givenCatStreamOfHelloWorld$whenPerformAsAction$thenPrinted() {
      performAction(
          cmd("cat")
              .stdin(Stream.of("hello", "world"))
              .downstreamConsumer(toStdoutAndGivenList(out))
              .toAction());
      assertThat(
          out,
          asListOf(String.class).equalTo(asList("hello", "world")).$()
      );
    }

    @Test
    public void givenPwd$whenPerformAsAction$thenPrinted() {
      performAction(
          cmd("pwd")
              .cwd(getCwd())
              .downstreamConsumer(toStdoutAndGivenList(out))
              .toAction());
      assertThat(
          out,
          asListOf(String.class).equalTo(singletonList(getCwd().getAbsolutePath())).$()
      );
    }

    @Test
    public void givenEchoHelloWithDownstreamConsumerFactory$whenPerformAsAction$thenPrintedBySpecifiedDownstreamConsumer() {
      List<String> downstream = new LinkedList<>();
      performAction(
          cmd("echo")
              .append("hello")
              .downstreamConsumer(toStdoutAndGivenList(out))
              .downstreamConsumerFactory(() -> downstream::add)
              .toAction());
      assertThat(
          downstream,
          asListOf(String.class).equalTo(singletonList("hello")).$()
      );
    }

    @Test
    public void givenEchoHelloWithTimeoutInOneSecond$whenPerformAsAction$thenPrinted() {
      performAction(
          cmd("echo hello")
              .retryOption(RetryOption.timeoutInSeconds(1))
              .downstreamConsumer(toStdoutAndGivenList(out))
              .toAction()
      );
      assertThat(
          out,
          asListOf(String.class).equalTo(singletonList("hello")).$()
      );
    }

    @Test
    public void givenEchoHelloWithTimeoutInOneMinutesAndRetryingTwice$whenPerformAsAction$thenPrinted() {
      performAction(
          cmd("echo hello")
              .retryOption(RetryOption.builder()
                  .timeoutIn(1, MINUTES)
                  .retries(2)
                  .retryOn(Exception.class)
                  .retryInterval(1, SECONDS).build())
              .downstreamConsumer(toStdoutAndGivenList(out))
              .toAction()
      );
      assertThat(
          out,
          asListOf(String.class).equalTo(singletonList("hello")).$()
      );
    }

    private static File getCwd() {
      return new File(System.getProperty("user.dir"));
    }

    private static Shell sh() {
      return new Shell() {
        @Override
        public String program() {
          return "sh";
        }

        @Override
        public List<String> options() {
          return singletonList("-c");
        }
      };
    }
  }

  public static class AsAction extends Base {
    @Test
    public void givenEchoHello$whenPerformAction$thenFinishNormally() {
      performAction(cmd("echo hello")
          .downstreamConsumer(toStdoutAndGivenList(out))
          .toAction());
      assertThat(
          out,
          asListOf(String.class).contains("hello").$()
      );
    }

    @Test
    public void givenEchoHello$whenExpectHelloInStdout$thenFinishNormally() {
      performAction(cmd("echo hello")
          .downstreamConsumer(toStdoutAndGivenList(out))
          .checker(createProcessStreamerCheckerForCmdTest("hello"))
          .toAction());
      assertThat(
          report,
          asListOf(String.class).anyMatch(Printable.predicate("contains[hello]", s -> s.contains("echo hello"))).$()
      );
    }

    @Test(expected = ProcessStreamer.Failure.class)
    public void givenEchoWorld$whenExpectHelloInStdout$thenFailureIsThrown() {
      try {
        performAction(cmd("echo world")
            .downstreamConsumer(toStdoutAndGivenList(out))
            .checker(createProcessStreamerCheckerForCmdTest("hello"))
            .toAction());
      } catch (ProcessStreamer.Failure failure) {
        String keywordSearchedFor = "hello";
        String actualOutput = "world";
        assertThat(
            failure.getMessage(),
            asString(substringAfterRegex("isPresent\\[" + keywordSearchedFor + "\\]")
                .after("was not met").$())
                .containsString(actualOutput).$()
        );
        throw failure;
      }
    }

    @Test(expected = ProcessStreamer.Failure.class)
    public void givenEchoHelloAndUnknownCommand$whenExpectHelloInStdout$thenFailureIsThrown() {
      try {
        performAction(cmd("echo hello && __unknownCommand__").downstreamConsumer(toStdoutAndGivenList(out)).checker(createProcessStreamerCheckerForCmdTest("hello")).toAction());
      } catch (ProcessStreamer.Failure failure) {
        System.out.println(failure.getMessage());
        assertThat(
            failure.getMessage(),
            allOf(
                asString(substringAfterRegex("Expectation for exit code").after("was not met").after("actual exit code").$()).containsString("127").$(),
                asString(substringAfterRegex("Recent output").after("__unknownCommand__").$()).containsString("not found").$(),
                asString(substringAfterRegex("Recent output").$()).containsString("hello").$()
            ));
        throw failure;
      }
    }

    @Test(expected = ProcessStreamer.Failure.class)
    public void givenUnknownCommand$whenPerformAsAction$thenFailureIsThrown() {
      try {
        performAction(
            forEach("i", StreamGenerator.fromArray("hello", "world")).perform(
                i -> cmd("UNKNOWN_COMMAND")
                    .appendVariable(i)
                    .downstreamConsumer(toStdoutAndGivenList(out)).$()));
      } catch (ProcessStreamer.Failure failure) {
        assertThat(
            failure.getMessage(),
            asString().containsString("UNKNOWN_COMMAND hello").$()
        );
        throw failure;
      }
    }

    @Test
    public void givenEchoVariable_i_usingManuallyWrittenPlaceHolder$whenPerformAsActionInsideHelloWorldLoop$thenBothHelloAndWorldFoundInOutput() {
      performAction(
          forEach("i", StreamGenerator.fromArray("hello", "world")).perform(
              i -> cmd("echo {{0}}", i)
                  .downstreamConsumer(toStdoutAndGivenList(out))
                  .$()));
      assertThat(
          out,
          asListOf(
              String.class,
              sublistAfterElement("hello").afterElement("world").$())
              .isEmpty().$()
      );
    }

    @Test
    public void givenEchoVariable_i_usingManuallyWrittenPlaceHolderByName$whenPerformAsActionInsideHelloWorldLoop$thenBothHelloAndWorldFoundInOutput() {
      performAction(
          forEach("i", StreamGenerator.fromArray("hello", "world"))
              .perform(i -> cmd("echo {{i}}", config(PlaceHolderFormatter.PLACE_HOLDER_FORMATTER_BY_NAME), i)
                  .downstreamConsumer(toStdoutAndGivenList(out))
                  .$()));
      assertThat(
          out,
          asListOf(String.class, sublistAfterElement("hello").afterElement("world").$()).isEmpty().$()
      );
    }

    private static CommanderConfig config(final Function<ContextVariable[], IntFunction<String>> placeHolderFormatter) {
      return CommanderConfig.builder()
          .placeHolderFormatter(placeHolderFormatter)
          .build();
    }

    @Test
    public void givenEchoVariable_i_$whenPerformAsActionInsideHelloWorldLoop$thenBothHelloAndWorldFoundInOutput() {
      performAction(forEach("i", StreamGenerator.fromArray("hello", "world"))
          .perform(i -> cmd("echo", config(PlaceHolderFormatter.DEFAULT_PLACE_HOLDER_FORMATTER))
              .appendQuotedVariable(i)
              .downstreamConsumer(toStdoutAndGivenList(out))
              .$()));
    }

  }

  public static class AsStreamGenerator extends Base {
    @Test
    public void givenEchoHelloEchoWorld$whenUseAsStreamGenerator$thenBothHelloAndWorldFoundInOutput() {
      performAction(
          forEach("i",
              cmd("echo hello && echo world").downstreamConsumer(toStdoutAndGivenList(out)).toStreamGenerator()
          ).perform(i ->
              leaf(ContextConsumer.of(
                  () -> "print 'i'",
                  context -> System.out.println("i=" + i.resolve(context))))
          ));
      assertThat(
          out,
          asListOf(String.class).contains("hello").contains("world").$()
      );
    }

    @Test(expected = ProcessStreamer.Failure.class)
    public void givenEchoHelloEchoWorld$whenUseAsStreamGeneratorExpectingUnknownKeyowrd$thenFailureIsThrown() {
      String keyword = "UNKNOWN";
      try {
        performAction(
            forEach("i", cmd("echo hello && echo world").downstreamConsumer(toStdoutAndGivenList(out)).checker(createProcessStreamerCheckerForCmdTest(keyword)).toStreamGenerator())
                .perform(i -> leaf(ContextConsumer.of(
                    () -> "print 'i'",
                    context -> System.out.println("i=" + i.resolve(context))))));
      } catch (ProcessStreamer.Failure failure) {
        assertThat(
            failure.getMessage(),
            asString(substringAfterRegex("isPresent\\[" + keyword + "\\]").after("hello").after("world").$()).isNotNull().$()
        );
        throw failure;
      }
    }
  }

  public static class AsContextConsumer extends Base {
    @Test
    public void givenEchoVariable_i_usingManuallyWrittenPlaceHolder$whenPerformAsContextConsumerInsideHelloWorldLoop$thenFinishesNormally() {
      performAction(
          forEach("i", StreamGenerator.fromArray("hello", "world"))
              .perform(i -> leaf(
                  cmd("echo {{0}}").downstreamConsumer(toStdoutAndGivenList(out))
                      .downstreamConsumer(toStdoutAndGivenList(out))
                      .toContextConsumer())
              ));
    }

    @Test(expected = ProcessStreamer.Failure.class)
    public void givenEchoVariable_i_usingManuallyWrittenPlaceHolder$whenPerformAsContextConsumerInsideHelloWorldLoopExpectingUnknownKeyword$thenFailureIsThrown() {
      String keyword = "UNKNOWN";
      try {
        performAction(
            forEach("i", StreamGenerator.fromArray("hello", "world"))
                .perform(i -> leaf(cmd("echo {{0}}", i)
                    .downstreamConsumer(toStdoutAndGivenList(out))
                    .checker(createProcessStreamerCheckerForCmdTest(keyword))
                    .downstreamConsumer(toStdoutAndGivenList(out))
                    .toContextConsumer())
                ));
      } catch (ProcessStreamer.Failure failure) {
        failure.printStackTrace();
        assertThat(
            failure.getMessage(),
            asString(substringAfterRegex("isPresent\\[" + keyword + "\\]").after("hello").$()).isNotNull().$()
        );
        throw failure;
      }
    }
  }

  public static class AsContextFunction extends Base {
    @Test
    public void givenEchoVariable_i$whenPerformAsContextFunctionInsideHelloWorldLoop$thenFinishesNormally() {
      performAction(
          forEach("i", StreamGenerator.fromArray("hello", "world"))
              .perform(i ->
                  leaf(c -> System.out.println("out=<" +
                      cmd("echo").appendVariable(i).downstreamConsumer(toStdoutAndGivenList(out))
                          .downstreamConsumer(toStdoutAndGivenList(out))
                          .toContextFunction()
                          .apply(c) + ">"))));
    }

    @Test(expected = ProcessStreamer.Failure.class)
    public void givenEchoVariable_i$whenPerformAsContextFunctionInsideHelloWorldLoopExpectingUnknownKeyword$thenFailureIsThrown() {
      String keyword = "UNKNOWN";
      try {
        performAction(
            forEach("i", StreamGenerator.fromArray("hello", "world"))
                .perform(i ->
                    leaf(c -> System.out.println("out=<" +
                        cmd("echo")
                            .appendVariable(i)
                            .checker(createProcessStreamerCheckerForCmdTest(keyword))
                            .downstreamConsumer(toStdoutAndGivenList(out))
                            .toContextFunction()
                            .apply(c) + ">"))));
      } catch (ProcessStreamer.Failure failure) {
        assertThat(
            failure.getMessage(),
            asString(substringAfterRegex("isPresent\\[" + keyword + "\\]").after("hello").$()).isNotNull().$()
        );
        throw failure;
      }
    }
  }

  public static class AsContextPredicate extends Base {
    @Test
    public void givenEchoVariable_i$whenPerformAsContextPredicateExpectingHelloInsideHelloWorldLoop$thenMetAndNotMet() {
      String keyword = "hello";
      performAction(
          forEach("i", StreamGenerator.fromArray("hello", "world"))
              .perform(i1 -> when(cmd("echo").appendVariable(i1).checkerFactory(() -> createProcessStreamerCheckerForCmdTest(keyword)).toContextPredicate())
                  .perform(leaf(c -> out.add("MET")))
                  .otherwise(leaf(c -> out.add("NOTMET")))));
      System.out.println(out);
      assertThat(
          out,
          asListOf(String.class, sublistAfterElement("MET").afterElement("NOTMET").$()).$()
      );
    }

    @Test
    public void givenEchoVariable_i$whenPerformAsContextPredicateExpectingWorldInsideHelloWorldLoop$thenNotMetAndMet() {
      String keyword = "world";
      performAction(
          forEach("i", StreamGenerator.fromArray("hello", "world"))
              .perform(i1 -> when(cmd("echo")
                  .appendVariable(i1)
                  .checkerFactory(() -> createProcessStreamerCheckerForCmdTest(keyword)).toContextPredicate())
                  .perform(leaf(c -> out.add("MET")))
                  .otherwise(leaf(c -> out.add("NOTMET")))));
      System.out.println(out);
      assertThat(
          out,
          asListOf(String.class, sublistAfterElement("NOTMET").afterElement("MET").$()).$()
      );
    }

    @Test
    public void givenEchoVariable_i$whenPerformAsContextPredicateExpectingUnknownKeywordInsideHelloWorldLoop$thenNotMetAndNotMet() {
      String keyword = "UNKNOWN";
      performAction(
          forEach("i", StreamGenerator.fromArray("hello", "world"))
              .perform(i -> when(cmd("echo")
                  .appendVariable(i)
                  .checkerFactory(() -> createProcessStreamerCheckerForCmdTest(keyword)).toContextPredicate())
                  .perform(leaf(c -> out.add("MET")))
                  .otherwise(leaf(c -> out.add("NOTMET")))));
      System.out.println(out);
      assertThat(
          out,
          asListOf(String.class, sublistAfterElement("NOTMET").afterElement("NOTMET").$()).$()
      );
    }
  }

  private static ProcessStreamer.Checker createProcessStreamerCheckerForCmdTest(final String stdoutKeyword) {
    return new ProcessStreamer.Checker() {
      final StreamChecker forStdOut = streamCheckerForKeywordPresence(stdoutKeyword);
      final StreamChecker forStdErr = emptyStreamChecker();
      final Predicate<Integer> exitCodeChecker = Printable.predicate("==[0]", i -> i == 0);

      @Override
      public StreamChecker forStdOut() {
        return forStdOut;
      }

      @Override
      public StreamChecker forStdErr() {
        return forStdErr;
      }

      @Override
      public Predicate<Integer> exitCodeChecker() {
        return exitCodeChecker;
      }

      @Override
      public String toString() {
        return String.format("exitCode: %s; stdout: %s; stderr: %s", exitCodeChecker, forStdOut, forStdErr);
      }
    };
  }

  private static ProcessStreamer.Checker.StreamChecker streamCheckerForKeywordPresence(final String keyword) {
    return new ProcessStreamer.Checker.StreamChecker() {
      boolean ret = false;

      @Override
      public boolean getAsBoolean() {
        return ret;
      }

      @Override
      public void accept(String s) {
        if (s.contains(keyword)) {
          ret = true;
        }
      }

      @Override
      public String toString() {
        return "isPresent[" + keyword + "]";
      }
    };
  }

  private static ProcessStreamer.Checker.StreamChecker emptyStreamChecker() {
    return new ProcessStreamer.Checker.StreamChecker() {
      @Override
      public boolean getAsBoolean() {
        return true;
      }

      @Override
      public void accept(String s) {
      }

      @Override
      public String toString() {
        return "alwaysTrue";
      }
    };
  }

  private static Consumer<String> toStdoutAndGivenList(List<String> out) {
    return ((Consumer<String>) System.out::println).andThen(out::add);
  }
}
