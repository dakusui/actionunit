package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.actionunit.actions.RetryOption;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.context.*;
import com.github.dakusui.actionunit.core.context.multiparams.Params;
import com.github.dakusui.printables.Printables;
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
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

enum CommodoreUtils {
  ;

  private static final Logger LOGGER = LoggerFactory.getLogger(CommodoreUtils.class);

  static Action createAction(Commodore commodore, CommandLineComposer commandLineComposer, String[] variableNames) {
    return RetryOption.retryAndTimeOut(
        leaf(createContextConsumer(commodore, commandLineComposer, variableNames)),
        commodore.retryOption());
  }

  static StreamGenerator<String> createStreamGenerator(
      Commodore<?> commodore,
      final CommandLineComposer commandLineComposer,
      final String[] variableNames) {
    return StreamGenerator.fromContextWith(
        new Function<Params, Stream<String>>() {
          @Override
          public Stream<String> apply(Params params) {
            return createProcessStreamerBuilder(commodore, params, commandLineComposer)
                .checker(commodore.checker())
                .build()
                .stream()
                .peek(commodore.downstreamConsumer());
          }

          @Override
          public String toString() {
            return format("(%s)", commodore.buildCommandLineComposer().apply(variableNames));
          }
        },
        variableNames
    );
  }

  static ContextConsumer createContextConsumer(
      Commodore<?> commodore,
      CommandLineComposer commandLineComposer, String[] variableNames) {
    requireNonNull(commodore);
    return multiParamsConsumerFor(variableNames)
        .toContextConsumer(printableConsumer(
            (Params params) -> createProcessStreamerBuilder(
                commodore,
                params, commodore.buildCommandLineComposer())
                .checker(commodore.checker())
                .build()
                .stream()
                .forEach(commodore.downstreamConsumer()))
            .describe(commandLineComposer::commandLineString));
  }

  static ContextPredicate createContextPredicate(
      Commodore<?> commodore,
      CommandLineComposer commandLineComposer,
      String[] variableNames) {
    return multiParamsPredicateFor(variableNames)
        .toContextPredicate(Printables.printablePredicate(
            (Params params) -> {
              try {
                int exitCode = createProcessStreamerBuilder(commodore, params, commandLineComposer)
                    .checker(commodore.checker())
                    .build()
                    .waitFor();
                LOGGER.debug("Exit code was: {}", exitCode);
                return true;
              } catch (ProcessStreamer.Failure failure) {
                String msg = format("Condition '%s' was not satisfied: %s",
                    commodore.checker(),
                    failure.getMessage());
                LOGGER.debug(msg);
                LOGGER.trace(msg, failure);
                return false;
              } catch (InterruptedException e) {
                throw new RuntimeException(e);
              }
            })
            .describe(() -> format(
                "Exit code of '%s': %s",
                commodore.buildCommandLineComposer().commandLineString(),
                objectToStringIfOverridden(commodore.checker(), () -> "(noname)"))));
  }

  static ContextFunction<String> createContextFunction(Commodore<?> commodore, CommandLineComposer commandLineComposer, String[] variableNames) {
    return ContextFunctions.<String>multiParamsFunctionFor(variableNames)
        .toContextFunction(params ->
            createProcessStreamerBuilder(commodore, params, commandLineComposer)
                .checker(commodore.checker())
                .build()
                .stream()
                .peek(commodore.downstreamConsumer())
                .collect(joining(format("%n"))));
  }

  static ProcessStreamer.Builder createProcessStreamerBuilder(Commodore<?> commodore, Params params, CommandLineComposer commandLineComposer) {
    return createProcessStreamerBuilder(
        commodore.stdin(),
        commodore.shell(),
        commodore.cwd().orElse(null),
        commodore.envvars(),
        commandLineComposer,
        params
    );
  }

  static ProcessStreamer.Builder createProcessStreamerBuilder(
      Stream<String> stdin, Shell shell, File cwd, Map<String, String> envvars,
      CommandLineComposer commandLineComposer,
      Params params) {
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
    if (cwd != null)
      ret.cwd(cwd);
    return ret.command(commandLine);
  }
}
