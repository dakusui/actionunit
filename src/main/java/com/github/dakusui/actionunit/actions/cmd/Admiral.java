package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.ContextConsumer;
import com.github.dakusui.actionunit.core.ContextFunctions.Params;
import com.github.dakusui.actionunit.core.ContextPredicate;
import com.github.dakusui.actionunit.core.StreamGenerator;
import com.github.dakusui.cmd.core.process.ProcessStreamer;
import com.github.dakusui.cmd.core.process.ProcessStreamer.Checker;
import com.github.dakusui.cmd.core.process.Shell;
import com.github.dakusui.printables.Printables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Formattable;
import java.util.Formatter;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.github.dakusui.actionunit.core.ActionSupport.leaf;
import static com.github.dakusui.actionunit.utils.InternalUtils.objectToStringIfOverridden;
import static com.github.dakusui.cmd.core.process.ProcessStreamer.Checker.createCheckerForExitCode;
import static com.github.dakusui.printables.Printables.consumer;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class Admiral {

  private static final Logger LOGGER = LoggerFactory.getLogger(Admiral.class);
  private              Shell  shell;

  public Admiral(Shell shell) {
    this.shell = shell;
  }

  public Action toActionWith(CommandLineComposer commandLineComposer, String... variableNames) {
    return toActionWith(commandLineComposer, Checker.createCheckerForExitCode(0), variableNames);
  }

  public Action toActionWith(CommandLineComposer commandLineComposer, Checker checker, String... variableNames) {
    requireNonNull(commandLineComposer);
    return leaf(toContextConsumer(commandLineComposer, checker, variableNames));
  }

  public ContextConsumer toContextConsumer(CommandLineComposer commandLineComposer, String... variableNames) {
    return toContextConsumer(commandLineComposer, Checker.createCheckerForExitCode(0), variableNames);
  }

  public ContextConsumer toContextConsumer(CommandLineComposer commandLineComposer, Checker checker, String... variableNames) {
    return new ContextConsumer.Builder(variableNames)
        .with(consumer(
            (Params params) -> createProcessStreamerBuilder(commandLineComposer, params)
                .checker(checker)
                .build()
                .stream()
                .forEach(LOGGER::trace))
            .describe(commandLineComposer::commandLineString));
  }

  public ContextPredicate toContextPredicate(CommandLineComposer commandLineComposer, Predicate<Integer> exitCodeChecker, String... variableNames) {
    return new ContextPredicate.Builder(variableNames)
        .with(Printables.predicate(
            (Params params) -> {
              try {
                return exitCodeChecker.test(createProcessStreamerBuilder(commandLineComposer, params)
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
                objectToStringIfOverridden(exitCodeChecker, "(noname)"))));
  }

  public StreamGenerator<String> toStreamGenerator(CommandLineComposer commandLineComposer, String... variableNames) {
    return toStreamGenerator(commandLineComposer, Checker.createCheckerForExitCode(0), variableNames);
  }

  public StreamGenerator<String> toStreamGenerator(CommandLineComposer commandLineComposer, Checker checker, String... variableNames) {
    return StreamGenerator.createFromContextWith(
        new Function<Params, Stream<String>>() {
          @Override
          public Stream<String> apply(Params params) {
            return createProcessStreamerBuilder(commandLineComposer, params)
                .checker(checker)
                .build()
                .stream();
          }

          @Override
          public String toString() {
            return format("(%s)", commandLineComposer.apply(variableNames));
          }
        },
        variableNames
    );
  }

  private ProcessStreamer.Builder createProcessStreamerBuilder(CommandLineComposer commandLineComposer, Params params) {
    String commandLine = commandLineComposer.apply(
        params.paramNames()
            .stream()
            .map(params::valueOf)
            .toArray());
    LOGGER.info("Command Line:{}", commandLine);
    return ProcessStreamer.source(shell).command(commandLine);
  }


  public interface CommandLineComposer extends Function<Object[], String>, Formattable {
    @Override
    default String apply(Object[] params) {
      return MessageFormat.format(commandLineString(), (Object[]) params);
    }

    @Override
    default void formatTo(Formatter formatter, int flags, int width, int precision) {
      formatter.format(commandLineString());
    }

    String commandLineString();
  }

}