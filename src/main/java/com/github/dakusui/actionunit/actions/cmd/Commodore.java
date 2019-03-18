package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.actionunit.actions.RetryOption;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.context.ContextConsumer;
import com.github.dakusui.actionunit.core.context.ContextFunction;
import com.github.dakusui.actionunit.core.context.ContextPredicate;
import com.github.dakusui.actionunit.core.context.StreamGenerator;
import com.github.dakusui.processstreamer.core.process.ProcessStreamer.Checker;
import com.github.dakusui.processstreamer.core.process.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.stream.Stream;

import static com.github.dakusui.actionunit.utils.Checks.requireState;
import static java.util.Objects.requireNonNull;

public abstract class Commodore<C extends Commodore<C>> {
  private static final Logger              LOGGER = LoggerFactory.getLogger(Commodore.class);
  private final        IntFunction<String> parameterPlaceHolderFactory;

  private       RetryOption                 retryOption;
  private       Consumer<String>            downstreamConsumer;
  private       Checker                     checker;
  private       Stream<String>              stdin;
  private       Shell                       shell;
  private       File                        cwd = null;
  private final Map<String, String>         envvars;
  private       CommandLineComposer.Builder commandLineComposerBuilder;


  protected Commodore(IntFunction<String> parameterPlaceHolderFactory) {
    this.parameterPlaceHolderFactory = requireNonNull(parameterPlaceHolderFactory);
    this.envvars = new LinkedHashMap<>();
    this.stdin(Stream.empty())
        .retryOption(RetryOption.none())
        .shell(Shell.local())
        .checker(Checker.createCheckerForExitCode(0))
        .downstreamConsumer(LOGGER::trace);
  }

  @SuppressWarnings("unchecked")
  public C retryOption(RetryOption retryOption) {
    this.retryOption = retryOption;
    return (C) this;
  }

  @SuppressWarnings("unchecked")
  public C downstreamConsumer(Consumer<String> downstreamConsumer) {
    this.downstreamConsumer = requireNonNull(downstreamConsumer);
    return (C) this;
  }

  @SuppressWarnings("unchecked")
  public C checker(Checker checker) {
    this.checker = requireNonNull(checker);
    return (C) this;
  }

  @SuppressWarnings("unchecked")
  public C stdin(Stream<String> stdin) {
    this.stdin = requireNonNull(stdin);
    return (C) this;
  }

  @SuppressWarnings("unchecked")
  public C shell(Shell shell) {
    this.shell = requireNonNull(shell);
    return (C) this;
  }

  @SuppressWarnings("unchecked")
  public C cwd(File cwd) {
    this.cwd = requireNonNull(cwd);
    return (C) this;
  }

  @SuppressWarnings("unchecked")
  public C setenv(String varname, String varvalue) {
    this.envvars.put(requireNonNull(varname), requireNonNull(varvalue));
    return (C) this;
  }

  public Action toAction() {
    return CommodoreUtils.createAction(this, this.buildCommandLineComposer(), this.variableNames());
  }

  public StreamGenerator<String> toStreamGenerator() {
    return CommodoreUtils.createStreamGenerator(this, this.buildCommandLineComposer(), this.variableNames());
  }

  public ContextConsumer toContextConsumer() {
    return CommodoreUtils.createContextConsumer(this, this.buildCommandLineComposer(), this.variableNames());
  }

  public ContextPredicate toContextPredicate() {
    return CommodoreUtils.createContextPredicate(this, this.buildCommandLineComposer(), this.variableNames());
  }

  public ContextFunction<String> toContextFunction() {
    return CommodoreUtils.createContextFunction(this, this.buildCommandLineComposer(), this.variableNames());
  }

  public C command(String command, String... variableNames) {
    this.knownVariables(variableNames).append(command);
    return (C) this;
  }

  public C knownVariables(String... variableNames) {
    requireState(Objects::isNull, this.commandLineComposerBuilder);
    this.commandLineComposerBuilder = new CommandLineComposer.Builder(this.parameterPlaceHolderFactory(), variableNames);
    return (C) this;
  }

  public C append(String text) {
    this.commandLineComposerBuilder.append(text);
    return (C) this;
  }


  public C appendVariable(String variableName) {
    this.commandLineComposerBuilder.appendVariable(variableName);
    return (C) this;
  }

  protected CommandLineComposer buildCommandLineComposer() {
    return requireNonNull(this.commandLineComposerBuilder).build();
  }

  protected String[] variableNames() {
    return requireNonNull(this.commandLineComposerBuilder).knownVariables();
  }

  protected IntFunction<String> parameterPlaceHolderFactory() {
    return this.parameterPlaceHolderFactory;
  }


  RetryOption retryOption() {
    return this.retryOption;
  }

  Consumer<String> downstreamConsumer() {
    return this.downstreamConsumer;
  }

  Checker checker() {
    return this.checker;
  }

  Stream<String> stdin() {
    return this.stdin;
  }

  Shell shell() {
    return this.shell;
  }

  Optional<File> cwd() {
    return Optional.ofNullable(this.cwd);
  }

  Map<String, String> envvars() {
    return this.envvars;
  }

  public static class Simple extends Commodore<Simple> {
    protected Simple(IntFunction<String> parameterPlaceHolderFactory) {
      super(parameterPlaceHolderFactory);
    }
  }

  public interface Factory {
    Simple simple();
  }
}
