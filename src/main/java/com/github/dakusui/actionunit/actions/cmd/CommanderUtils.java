package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.actionunit.actions.RetryOption;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.context.*;
import com.github.dakusui.actionunit.core.context.multiparams.Params;
import com.github.dakusui.processstreamer.core.process.ProcessStreamer;
import com.github.dakusui.processstreamer.core.process.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.dakusui.actionunit.core.ActionSupport.leaf;
import static com.github.dakusui.actionunit.core.context.ContextFunctions.multiParamsConsumerFor;
import static com.github.dakusui.actionunit.core.context.ContextFunctions.multiParamsPredicateFor;
import static com.github.dakusui.actionunit.utils.InternalUtils.objectToStringIfOverridden;
import static com.github.dakusui.printables.Printables.printableConsumer;
import static com.github.dakusui.printables.Printables.printablePredicate;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

public enum CommanderUtils {
  ;

  private static final Logger LOGGER = LoggerFactory.getLogger(CommanderUtils.class);

  public static String quoteWithApostropheForShell(String s) {
    return String.format("'%s'", escapeSingleQuotesForShell(s));
  }

  private static String escapeSingleQuotesForShell(String s) {
    return requireNonNull(s).replaceAll("('+)", "'\"$1\"'");
  }

  static Action createAction(Commander commander, String[] variableNames) {
    return RetryOption.retryAndTimeOut(
        leaf(createContextConsumer(commander)),
        commander.retryOption());
  }

  static StreamGenerator<String> createStreamGenerator(
      Commander<?> commander) {
    return StreamGenerator.fromContextWith(
        new Function<Params, Stream<String>>() {
          @Override
          public Stream<String> apply(Params params) {
            return createProcessStreamerBuilder(commander, params)
                .checker(commander.checker())
                .build()
                .stream()
                .peek(commander.downstreamConsumer());
          }

          @Override
          public String toString() {
            return format("(%s)", commander.buildCommandLineComposer().commandLineString());
          }
        },
        commander.variableNames()
    );
  }

  static ContextConsumer createContextConsumer(Commander<?> commander) {
    requireNonNull(commander);
    return multiParamsConsumerFor(commander.variableNames())
        .toContextConsumer(
            printableConsumer(
                (Params params) -> createProcessStreamerBuilder(commander, params)
                    .checker(commander.checker())
                    .build()
                    .stream()
                    .forEach(commander.downstreamConsumer()))
                .describe(() -> commander.buildCommandLineComposer().commandLineString()));
  }

  static ContextPredicate createContextPredicate(
      Commander<?> commander) {
    return multiParamsPredicateFor(commander.variableNames())
        .toContextPredicate(printablePredicate(
            (Params params) -> {
              ProcessStreamer.Builder processStreamerBuilder = createProcessStreamerBuilder(commander, params);
              try {
                processStreamerBuilder
                    .checker(commander.checker())
                    .build()
                    .stream()
                    .forEach(commander.downstreamConsumer());
                return true;
              } catch (ProcessStreamer.Failure failure) {
                String msg = format("Condition '%s' was not satisfied: %s", commander.checker(), failure.getMessage());
                LOGGER.debug(msg);
                LOGGER.trace(msg, failure);
                return false;
              }
            })
            .describe(() -> format(
                "outputOf[command:'%s'].matches[%s]",
                commander.buildCommandLineComposer().commandLineString(),
                objectToStringIfOverridden(commander.checker(), () -> "(noname)"))));
  }

  static ContextFunction<String> createContextFunction(Commander<?> commander) {
    return ContextFunctions.<String>multiParamsFunctionFor(commander.variableNames())
        .toContextFunction(params ->
            createProcessStreamerBuilder(commander, params)
                .checker(commander.checker())
                .build()
                .stream()
                .peek(commander.downstreamConsumer())
                .collect(joining(format("%n"))));
  }

  static ProcessStreamer.Builder createProcessStreamerBuilder(Commander<?> commander, Params params) {
    return createProcessStreamerBuilder(
        commander.stdin(),
        commander.shell(),
        commander.cwd().orElse(null),
        commander.envvars(),
        commander.buildCommandLineComposer(),
        params
    );
  }

  static ProcessStreamer.Builder createProcessStreamerBuilder(
      Stream<String> stdin, Shell shell, File cwd, Map<String, String> envvars,
      CommandLineComposer commandLineComposer,
      Params params) {
    String[] variableNames = params.paramNames().toArray(new String[0]);
    Object[] variableValues = params.paramNames()
        .stream()
        .map(params::valueOf)
        .toArray();
    String commandLine = commandLineComposer
        .apply(variableNames)
        .apply(params.context(), variableValues);
    LOGGER.info("Command Line:{}", commandLine);
    LOGGER.debug("Shell:{}", shell);
    LOGGER.debug("Cwd:{}", cwd);
    LOGGER.debug("Environment variables:{}", envvars);
    ProcessStreamer.Builder ret;
    if (stdin == null)
      ret = ProcessStreamer.source(shell);
    else
      ret = ProcessStreamer.pipe(stdin, shell);
    envvars.forEach(ret::env);
    if (cwd != null)
      ret.cwd(cwd);
    return ret.command(commandLine);
  }
}
