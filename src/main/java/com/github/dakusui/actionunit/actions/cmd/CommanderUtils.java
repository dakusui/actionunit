package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.actionunit.actions.ContextVariable;
import com.github.dakusui.actionunit.actions.RetryOption;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.context.ContextFunctions;
import com.github.dakusui.actionunit.core.context.StreamGenerator;
import com.github.dakusui.actionunit.core.context.multiparams.Params;
import com.github.dakusui.processstreamer.core.process.ProcessStreamer;
import com.github.dakusui.processstreamer.core.process.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.github.dakusui.actionunit.core.ActionSupport.leaf;
import static com.github.dakusui.actionunit.core.context.ContextFunctions.multiParamsConsumerFor;
import static com.github.dakusui.actionunit.core.context.ContextFunctions.multiParamsPredicateFor;
import static com.github.dakusui.actionunit.utils.InternalUtils.toStringIfOverriddenOrNoname;
import static com.github.dakusui.printables.PrintableFunctionals.printableConsumer;
import static com.github.dakusui.printables.PrintableFunctionals.printablePredicate;
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

  static Action createAction(Commander<?> commander) {
    return RetryOption.retryAndTimeOut(
        leaf(createContextConsumer(commander)),
        commander.retryOption());
  }

  static Function<Context, Stream<String>> createStreamGenerator(
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
            return format("(%s)", commander.buildCommandLineComposer().format());
          }
        },
        commander.variables()
    );
  }

  static Consumer<Context> createContextConsumer(Commander<?> commander) {
    requireNonNull(commander);
    return multiParamsConsumerFor(commander.variables())
        .toContextConsumer(
            printableConsumer(
                (Params params) -> createProcessStreamerBuilder(commander, params)
                    .checker(commander.checker())
                    .build()
                    .stream()
                    .forEach(commander.downstreamConsumer()))
                .describe(() -> commander.buildCommandLineComposer().format()));
  }

  static Predicate<Context> createContextPredicate(Commander<?> commander) {
    return multiParamsPredicateFor(commander.variables())
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
                commander.buildCommandLineComposer().format(),
                toStringIfOverriddenOrNoname(commander.checker()))));
  }

  static Function<Context, String> createContextFunction(Commander<?> commander) {
    return ContextFunctions.<String>multiParamsFunctionFor(commander.variables())
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
      Stream<String> stdin,
      Shell shell,
      File cwd,
      Map<String, String> envvars,
      CommandLineComposer commandLineComposer,
      Params params) {
    ContextVariable[] variables = params.parameters().toArray(new ContextVariable[0]); // values
    Object[] variableValues = params.parameters()// values
        .stream()
        .map(params::valueOf)
        .toArray();
    String commandLine = commandLineComposer
        .apply(variables)
        .apply(params.context(), variableValues);
    LOGGER.info("Command Line:{}", commandLine);
    LOGGER.trace("Shell:{}", shell);
    if (cwd != null)
      LOGGER.debug("Cwd:{}", cwd);
    if (!envvars.isEmpty())
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
