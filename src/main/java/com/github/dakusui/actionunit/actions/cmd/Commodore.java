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
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public abstract class Commodore<C extends Commodore<C>> {
  private static final Logger                     LOGGER = LoggerFactory.getLogger(Commodore.class);
  private final        CommandLineComposerFactory commandLineComposerFactory;

  private       CommandLineComposer commandLineComposer;
  private       RetryOption         retryOption;
  private       Consumer<String>    downstreamConsumer;
  private       Checker             checker;
  private       Stream<String>      stdin;
  private       Shell               shell;
  private       File                cwd;
  private final Map<String, String> envvars;
  private       String[]            variableNames;


  protected Commodore(CommandLineComposerFactory commandLineComposerFactory) {
    this.commandLineComposerFactory = commandLineComposerFactory;
    this.envvars = new LinkedHashMap<>();
    this.stdin(Stream.empty())
        .retryOption(RetryOption.none())
        .cwd(new File(System.getProperty("user.dir")))
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
    return CommodoreUtils.createAction(this, this.commandLineComposer(), this.variableNames());
  }

  public StreamGenerator<String> toStreamGenerator() {
    return CommodoreUtils.createStreamGenerator(this, this.commandLineComposer(), this.variableNames());
  }

  public ContextConsumer toContextConsumer() {
    return CommodoreUtils.createContextConsumer(this, this.commandLineComposer(), this.variableNames());
  }

  public ContextPredicate toContextPredicate() {
    return CommodoreUtils.createContextPredicate(this, this.commandLineComposer(), this.variableNames());
  }

  public ContextFunction<String> toContextFunction() {
    return CommodoreUtils.createContextFunction(this, this.commandLineComposer(), this.variableNames());
  }

  private CommandLineComposerFactory commandLineComposerFactory() {
    return this.commandLineComposerFactory;
  }

  @SuppressWarnings("unchecked")
  public C commandLineComposer(CommandLineComposer commandLineComposer) {
    this.commandLineComposer = requireNonNull(commandLineComposer);
    return (C) this;
  }

  protected String[] variableNames() {
    return this.variableNames;
  }

  protected C command(String commandLineString, String... variableNames) {
    this.variableNames = variableNames;
    return this.commandLineComposer(
        this.commandLineComposerFactory().apply(
            requireNonNull(commandLineString),
            variableNames));
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

  File cwd() {
    return this.cwd;
  }

  Map<String, String> envvars() {
    return this.envvars;
  }

  CommandLineComposer commandLineComposer() {
    return requireNonNull(this.commandLineComposer);
  }

  public interface Factory {
    <C extends Commodore<C>> C commodore(Class<C> klass);
  }
}
