package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.context.ContextConsumer;
import com.github.dakusui.actionunit.core.context.ContextPredicate;
import com.github.dakusui.actionunit.core.context.StreamGenerator;
import com.github.dakusui.actionunit.core.context.multiparams.Params;
import com.github.dakusui.printables.Printables;
import com.github.dakusui.processstreamer.core.process.ProcessStreamer;
import com.github.dakusui.processstreamer.core.process.ProcessStreamer.Checker;
import com.github.dakusui.processstreamer.core.process.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.github.dakusui.actionunit.core.ActionSupport.*;
import static com.github.dakusui.actionunit.core.context.ContextFunctions.contextConsumerFor;
import static com.github.dakusui.actionunit.core.context.ContextFunctions.contextPredicateFor;
import static com.github.dakusui.actionunit.utils.InternalUtils.objectToStringIfOverridden;
import static com.github.dakusui.printables.Printables.consumer;
import static com.github.dakusui.printables.Printables.isEqualTo;
import static com.github.dakusui.processstreamer.core.process.ProcessStreamer.Checker.createCheckerForExitCode;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public abstract class CommanderBase<C extends CommanderBase<C>> implements Commander<C> {

  private static final Logger                LOGGER  = LoggerFactory.getLogger(CommanderBase.class);
  private              Shell                 shell;
  private              Stream<String>        stdin   = null;
  private              Consumer<String>      downstreamConsumer;
  private              Map<String, String>   envvars = new LinkedHashMap<>();
  private              File                  cwd;
  private              Commodore.RetryOption retryOption;

  protected CommanderBase(Shell shell) {
    this.shell = requireNonNull(shell);
    this.stdoutConsumer(LOGGER::debug);
    this.retryOption = Commodore.RetryOption.timeoutInSeconds(60);
  }

  @SuppressWarnings("unchecked")
  public C cwd(File cwd) {
    this.cwd = requireNonNull(cwd);
    return (C) this;
  }

  @SuppressWarnings("unchecked")
  public C env(String varname, String varvalue) {
    this.envvars.put(requireNonNull(varname), requireNonNull(varvalue));
    return (C) this;
  }

  @SuppressWarnings("unchecked")
  public C stdin(Stream<String> stream) {
    this.stdin = requireNonNull(stream);
    return (C) this;
  }

  public Action toAction() {
    return toActionWith(createCheckerForExitCode(0));
  }

  public Action toActionWith(Checker checker) {
    Action action = leaf(toContextConsumerWith(checker));
    AtomicReference<Action> work = new AtomicReference<>(action);
    return retryOption()
        .filter(retryOption -> {
          if (retryOption.retries > 0)
            work.set(
                retry(work.get())
                    .on(retryOption.retryOn)
                    .times(retryOption.retries)
                    .withIntervalOf(retryOption.retryInterval, retryOption.retryIntervalTimeUnit)
                    .build()
            );
          return true;
        })
        .filter(retryOption -> {
          if (retryOption.timeoutDuration >= 0)
            work.set(timeout(work.get()).in(retryOption.timeoutDuration, retryOption.timeoutTimeUnit));
          return true;
        })
        .map(retryOption -> work.get())
        .orElse(action);
  }

  public ContextConsumer toContextConsumer() {
    return toContextConsumerWith(createCheckerForExitCode(0));
  }

  abstract protected CommandLineComposer commandLineComposer();

  abstract protected String[] variableNames();

  public ContextConsumer toContextConsumerWith(Checker checker) {
    return toContextConsumerWith(this.commandLineComposer(), checker, this.variableNames());
  }

  public ContextPredicate toContextPredicate() {
    return toContextPredicate(this.commandLineComposer(), isEqualTo(0), this.variableNames());
  }

  public ContextPredicate toContextPredicateWith(Predicate<Integer> exitCodeChecker) {
    return toContextPredicate(this.commandLineComposer(), exitCodeChecker, this.variableNames());
  }

  public StreamGenerator<String> toStreamGenerator() {
    return toStreamGeneratorWith(createCheckerForExitCode(0));
  }

  public StreamGenerator<String> toStreamGeneratorWith(Checker checker) {
    return toStreamGenerator(this.commandLineComposer(), checker, this.variableNames());
  }

  @SuppressWarnings("unchecked")
  public C stdoutConsumer(Consumer<String> stdoutConsumer) {
    this.downstreamConsumer = requireNonNull(stdoutConsumer);
    return (C) this;
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
            return createProcessStreamerBuilder(commandLineComposer, params, CommanderBase.this.stdin)
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
                return exitCodeChecker.test(
                    createProcessStreamerBuilder(commandLineComposer, params, this.stdin)
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

  private ContextConsumer toContextConsumerWith(
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

  private Optional<Commodore.RetryOption> retryOption() {
    return Optional.ofNullable(this.retryOption);
  }
}