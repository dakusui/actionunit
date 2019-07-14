package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.actionunit.actions.RetryOption;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.context.ContextConsumer;
import com.github.dakusui.actionunit.core.context.ContextFunction;
import com.github.dakusui.actionunit.core.context.ContextPredicate;
import com.github.dakusui.actionunit.core.context.StreamGenerator;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.processstreamer.core.process.ProcessStreamer.Checker;
import com.github.dakusui.processstreamer.core.process.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.github.dakusui.actionunit.core.ActionSupport.named;
import static com.github.dakusui.processstreamer.core.process.ProcessStreamer.Checker.createCheckerForExitCode;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public abstract class Commander<C extends Commander<C>> implements Cloneable {
  private static final Logger LOGGER = LoggerFactory.getLogger(Commander.class);
  CommandLineComposer.Builder commandLineComposerBuilder;
  private final Function<String[], IntFunction<String>> parameterPlaceHolderFactory;

  private       RetryOption                retryOption;
  private       Supplier<Consumer<String>> downstreamConsumerFactory;
  private       Supplier<Checker>          checkerFactory;
  private       Stream<String>             stdin;
  private       Shell                      shell;
  private       File                       cwd         = null;
  private final Map<String, String>        envvars;
  private       String                     description = null;


  protected Commander(CommanderInitializer initializer) {
    this.parameterPlaceHolderFactory = initializer.variablePlaceHolderFormatter();
    this.envvars = new LinkedHashMap<>();
    this.stdin(Stream.empty())
        .retryOption(RetryOption.none())
        .shell(Shell.local())
        .checker(createCheckerForExitCode(0))
        .downstreamConsumer(LOGGER::trace);
  }

  @SuppressWarnings("unchecked")
  public C describe(String descriptionSupplier) {
    this.description = descriptionSupplier;
    return (C) this;
  }

  @SuppressWarnings("unchecked")
  public C clone() {
    try {
      C ret = (C) super.clone();
      if (ret.commandLineComposerBuilder().isPresent())
        ret.commandLineComposerBuilder = ret.commandLineComposerBuilderIfSet().clone();
      return ret;
    } catch (CloneNotSupportedException e) {
      throw ActionException.wrap(e);
    }
  }

  @SuppressWarnings("unchecked")
  public C retryOption(RetryOption retryOption) {
    this.retryOption = retryOption;
    return (C) this;
  }

  /**
   * Sets a down-stream consumer to this builder object.
   * In case the down-stream consumer is stateful, use {@link Commander#downstreamConsumerFactory(Supplier)}
   * method instead and give a supplier that returns a new consumer object every time
   * when its {@code get()} method is called.
   *
   * @param downstreamConsumer A down-stream consumer.
   * @return This object
   */
  public C downstreamConsumer(Consumer<String> downstreamConsumer) {
    requireNonNull(downstreamConsumer);
    return this.downstreamConsumerFactory(() -> downstreamConsumer);
  }

  /**
   * Sets a downstream consumer's factory to this builder object.
   *
   * @param downstreamConsumerFactory A supplier of a down-stream consumer.
   * @return This object
   */
  @SuppressWarnings("unchecked")
  public C downstreamConsumerFactory(Supplier<Consumer<String>> downstreamConsumerFactory) {
    this.downstreamConsumerFactory = requireNonNull(downstreamConsumerFactory);
    return (C) this;
  }

  /**
   * Sets a checker to this builder object.
   * In case the checker is stateful, use {@link Commander#checkerFactory(Supplier)}
   * method instead and give a suuplier that returns a new checker object every
   * time when its {@code get()} method is called.
   *
   * @param checker A checker object
   * @return This object
   */
  public C checker(Checker checker) {
    requireNonNull(checker);
    return this.checkerFactory(() -> checker);
  }

  @SuppressWarnings("unchecked")
  public C checkerFactory(Supplier<Checker> checkerFactory) {
    this.checkerFactory = requireNonNull(checkerFactory);
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

  /**
   * A building method that returns an {@link Action} object.
   *
   * @return An action object.
   */
  public Action toAction() {
    Action action = CommanderUtils.createAction(this, this.variableNames());
    return this.description != null ?
        named(this.description, action) :
        action;
  }

  /**
   * A building method that returns {@link StreamGenerator} object.
   *
   * @return A stream generator object.
   */
  public StreamGenerator<String> toStreamGenerator() {
    return CommanderUtils.createStreamGenerator(this);
  }

  public ContextConsumer toContextConsumer() {
    return CommanderUtils.createContextConsumer(this);
  }

  public ContextPredicate toContextPredicate() {
    return CommanderUtils.createContextPredicate(this);
  }

  public ContextFunction<String> toContextFunction() {
    return CommanderUtils.createContextFunction(this);
  }

  /**
   * Adds a {@code target} to this object. A target may be a file, directory, or a
   * message. For instance, for a {@code cat} command, which usually processes
   * a file or files, this method will be used to add a file to be processed by
   * the command.
   * The value of the {@code target} parameter will be quoted.
   *
   * @param target A target string
   * @return This object
   */
  public C add(ContextFunction<String> target) {
    return this.append(" ").appendq(requireNonNull(target));
  }

  /**
   * Adds a {@code target} to this object. A target may be a file, directory, or a
   * message. For instance, for a {@code cat} command, which usually processes
   * a file or files, this method will be used to add a file to be processed by
   * the command.
   * The value of the {@code target} parameter will be quoted.
   *
   * @param target A target string
   * @return This object
   */
  public C add(String target) {
    return this.append(" ").appendq(requireNonNull(target));
  }

  public C append(ContextFunction<String> func) {
    return append(func, false);
  }

  public C appendq(ContextFunction<String> func) {
    return append(func, true);
  }

  @SuppressWarnings("unchecked")
  public C append(ContextFunction<String> func, boolean b) {
    commandLineComposerBuilderIfSet().append(func, b);
    return (C) this;
  }

  public C append(String text) {
    return append(text, false);
  }

  public C appendq(String text) {
    return append(text, true);
  }

  @SuppressWarnings("unchecked")
  public C append(String text, boolean b) {
    commandLineComposerBuilderIfSet().append(text, b);
    return (C) this;
  }

  public C addOption(String option) {
    return this.append(" ").append(option);
  }

  public C appendVariable(String variableName) {
    return appendVariable(variableName, false);
  }

  public C appendQuotedVariable(String variableName) {
    return appendVariable(variableName, true);
  }

  @SuppressWarnings("unchecked")
  public C appendVariable(String variableName, boolean b) {
    commandLineComposerBuilderIfSet().appendVariable(variableName, b);
    return (C) this;
  }

  @SuppressWarnings("unchecked")
  public C declareVariable(String variableName) {
    this.commandLineComposerBuilderIfSet().declareVariable(variableName);
    return (C) this;
  }

  public RetryOption retryOption() {
    return this.retryOption;
  }

  public Supplier<Checker> checkerFactory() {
    return this.checkerFactory;
  }

  public Supplier<Consumer<String>> downstreamConsumerFactory() {
    return this.downstreamConsumerFactory;
  }

  public Stream<String> stdin() {
    return this.stdin;
  }

  public Shell shell() {
    return this.shell;
  }

  public Map<String, String> envvars() {
    return Collections.unmodifiableMap(this.envvars);
  }

  public Optional<File> cwd() {
    return Optional.ofNullable(this.cwd);
  }

  public Function<String[], IntFunction<String>> parameterPlaceHolderFactory() {
    return this.parameterPlaceHolderFactory;
  }

  @Override
  public String toString() {
    return format(
        "%s:Shell:(%s), CommandLine(%s)",
        this.getClass().getSimpleName(),
        shell(),
        commandLineComposerBuilder);
  }

  @SuppressWarnings("unchecked")
  public C command(String command) {
    this.commandLineComposerBuilder = new CommandLineComposer.Builder(this.parameterPlaceHolderFactory())
        .append(command, false);
    return (C) this;
  }

  protected CommandLineComposer.Builder commandLineComposerBuilderIfSet() {
    return this.commandLineComposerBuilder().orElseThrow(IllegalStateException::new);
  }

  public CommandLineComposer buildCommandLineComposer() {
    return commandLineComposerBuilderIfSet().clone().build();
  }

  Checker checker() {
    return this.checkerFactory.get();
  }

  Consumer<String> downstreamConsumer() {
    return this.downstreamConsumerFactory.get();
  }

  String[] variableNames() {
    return commandLineComposerBuilderIfSet().knownVariables();
  }

  Optional<CommandLineComposer.Builder> commandLineComposerBuilder() {
    return Optional.ofNullable(commandLineComposerBuilder);
  }
}
