package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.context.ContextConsumer;
import com.github.dakusui.actionunit.core.context.ContextPredicate;
import com.github.dakusui.actionunit.core.context.Params;
import com.github.dakusui.actionunit.core.context.StreamGenerator;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.utils.StableTemplatingUtils;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import com.github.dakusui.printables.Printables;
import com.github.dakusui.processstreamer.core.process.ProcessStreamer;
import com.github.dakusui.processstreamer.core.process.ProcessStreamer.Checker;
import com.github.dakusui.processstreamer.core.process.Shell;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Formattable;
import java.util.Formatter;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.github.dakusui.actionunit.core.ActionSupport.forEach;
import static com.github.dakusui.actionunit.core.ActionSupport.leaf;
import static com.github.dakusui.actionunit.core.context.ContextFunctions.contextConsumerFor;
import static com.github.dakusui.actionunit.core.context.ContextFunctions.contextPredicateFor;
import static com.github.dakusui.actionunit.utils.InternalUtils.objectToStringIfOverridden;
import static com.github.dakusui.crest.Crest.asString;
import static com.github.dakusui.crest.Crest.assertThat;
import static com.github.dakusui.printables.Printables.consumer;
import static com.github.dakusui.printables.Printables.isEqualTo;
import static com.github.dakusui.processstreamer.core.process.ProcessStreamer.Checker.createCheckerForExitCode;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class Commodore {

  private static final Logger              LOGGER  = LoggerFactory.getLogger(Commodore.class);
  private              Shell               shell;
  private              CommandLineComposer commandLineComposer;
  private              String[]            variableNames;
  private              Stream<String>      stdin   = null;
  private              Consumer<String>    downstreamConsumer;
  private              Map<String, String> envvars = new LinkedHashMap<>();
  private              File                cwd;

  public Commodore() {
    this(Shell.local());
  }

  public Commodore(Shell shell) {
    this.shell = requireNonNull(shell);
    this.stdoutConsumer(LOGGER::debug);
  }

  public Commodore command(String commandLineFormat, String... variableNames) {
    requireNonNull(commandLineFormat);
    return command(() -> commandLineFormat, variableNames);
  }

  public Commodore command(CommandLineComposer commandLineComposer, String... variableNames) {
    this.commandLineComposer = requireNonNull(commandLineComposer);
    this.variableNames = variableNames;
    return this;
  }

  public Commodore cwd(File cwd) {
    this.cwd = requireNonNull(cwd);
    return this;
  }

  public Commodore env(String varname, String varvalue) {
    this.envvars.put(requireNonNull(varname), requireNonNull(varvalue));
    return this;
  }

  public Commodore stdin(Stream<String> stream) {
    this.stdin = requireNonNull(stream);
    return this;
  }

  public Action toAction() {
    return toActionWith(Checker.createCheckerForExitCode(0));
  }

  public Action toActionWith(Checker checker) {
    return leaf(toContextConsumerWith(checker));
  }

  public ContextConsumer toContextConsumer() {
    return toContextConsumerWith(Checker.createCheckerForExitCode(0));
  }

  public ContextConsumer toContextConsumerWith(Checker checker) {
    return toContextConsumer(this.commandLineComposer, checker, this.variableNames);
  }

  public ContextPredicate toContextPredicate() {
    return toContextPredicate(this.commandLineComposer, isEqualTo(0), this.variableNames);
  }

  public ContextPredicate toContextPredicateWith(Predicate<Integer> exitCodeChecker) {
    return toContextPredicate(this.commandLineComposer, exitCodeChecker, this.variableNames);
  }

  public StreamGenerator<String> toStreamGenerator() {
    return toStreamGeneratorWith(Checker.createCheckerForExitCode(0));
  }

  public StreamGenerator<String> toStreamGeneratorWith(Checker checker) {
    return toStreamGenerator(this.commandLineComposer, checker, this.variableNames);
  }

  public Commodore stdoutConsumer(Consumer<String> stdoutConsumer) {
    this.downstreamConsumer = requireNonNull(stdoutConsumer);
    return this;
  }

  @FunctionalInterface
  public interface CommandLineComposer extends Function<Object[], String>, Formattable {
    @Override
    default String apply(Object[] argValues) {
      return StableTemplatingUtils.template(
          commandLineString(),
          StableTemplatingUtils.toMapping(this::parameterPlaceHolderFor, argValues)
      );
    }

    @Override
    default void formatTo(Formatter formatter, int flags, int width, int precision) {
      formatter.format(commandLineString());
    }

    default String parameterPlaceHolderFor(int parameterIndex) {
      return "{{" + parameterIndex + "}}";
    }

    String commandLineString();

    static CommandLineComposer create(
        String commandLineFormat,
        Function<String, String> parameterPlaceHolderFactory,
        String... variableNames) {
      requireNonNull(commandLineFormat);
      return new CommandLineComposer() {
        @Override
        public String commandLineString() {
          return commandLineFormat;
        }

        @Override
        public String parameterPlaceHolderFor(int parameterIndex) {
          return parameterPlaceHolderFactory.apply(variableNames[parameterIndex]);
        }
      };
    }

    static CommandLineComposer create(
        String commandLineFormat,
        String... variableNames) {
      requireNonNull(commandLineFormat);
      return create(commandLineFormat, v -> String.format("{{%s}}", v), variableNames);
    }
  }

  private ProcessStreamer.Builder createProcessStreamerBuilder(CommandLineComposer commandLineComposer, Params params, Stream<String> stdin) {
    String commandLine = commandLineComposer.apply(
        params.paramNames()
            .stream()
            .map(params::valueOf)
            .toArray());
    LOGGER.info("Command Line:{}", commandLine);
    ProcessStreamer.Builder ret;
    if (stdin == null)
      ret = ProcessStreamer.source(shell);
    else
      ret = ProcessStreamer.pipe(stdin, shell);
    envvars.forEach(ret::env);
    return ret.command(commandLine).cwd(cwd);
  }

  private StreamGenerator<String> toStreamGenerator(CommandLineComposer commandLineComposer, Checker checker, String... variableNames) {
    return StreamGenerator.fromContextWith(
        new Function<Params, Stream<String>>() {
          @Override
          public Stream<String> apply(Params params) {
            return createProcessStreamerBuilder(commandLineComposer, params, Commodore.this.stdin)
                .checker(checker)
                .build()
                .stream()
                .peek(downstreamConsumer);
          }

          @Override
          public String toString() {
            return format("(%s)", commandLineComposer.apply(variableNames));
          }
        },
        variableNames
    );
  }

  private ContextPredicate toContextPredicate(
      CommandLineComposer commandLineComposer,
      Predicate<Integer> exitCodeChecker,
      String... variableNames) {
    return contextPredicateFor(variableNames)
        .with(Printables.predicate(
            (Params params) -> {
              try {
                return exitCodeChecker.test(createProcessStreamerBuilder(commandLineComposer, params, this.stdin)
                    .checker(createCheckerForExitCode(exitCode -> true))
                    .build()
                    .waitFor());
              } catch (InterruptedException e) {
                throw new RuntimeException(e);
              }
            })
            .describe(() -> format(
                "Exit code of '%s': %s",
                commandLineComposer.commandLineString(),
                objectToStringIfOverridden(exitCodeChecker, () -> "(noname)"))));
  }

  private ContextConsumer toContextConsumer(
      CommandLineComposer commandLineComposer,
      Checker checker,
      String... variableNames) {
    requireNonNull(commandLineComposer);
    requireNonNull(variableNames);
    return contextConsumerFor(variableNames)
        .with(consumer(
            (Params params) -> createProcessStreamerBuilder(commandLineComposer, params, this.stdin)
                .checker(checker)
                .build()
                .stream()
                .forEach(downstreamConsumer))
            .describe(commandLineComposer::commandLineString));
  }

  public static class CommodoreUnitTest {
    List<String> out = new LinkedList<>();

    @Test
    public void test() {
      ReportingActionPerformer.create().performAndReport(
          forEach("i", StreamGenerator.fromArray("Hello", "World"))
              .perform(
                  new Commodore()
                      .command("echo '{{0}}'", "i")
                      .stdoutConsumer(out::add)
                      .toAction()
              ),
          Writer.Std.OUT);
      assertThat(
          out,
          asString("get", 0).$()
      );
    }

    @Test
    public void test2() {
      ReportingActionPerformer.create().performAndReport(
          forEach("i", StreamGenerator.fromArray("Hello", "World"))
              .perform(
                  new Commodore(Shell.local())
                      .command("echo '{{0}}'", "i")
                      .stdoutConsumer(out::add)
                      .toAction()
              ),
          Writer.Std.OUT);
      assertThat(
          out,
          asString("get", 0).$()
      );
    }
  }
}