package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.actionunit.actions.ContextVariable;
import com.github.dakusui.actionunit.actions.RetryOption;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.context.StreamGenerator;
import com.github.dakusui.actionunit.exceptions.ActionException;
import com.github.dakusui.processstreamer.core.process.ProcessStreamer.Checker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.*;
import java.util.stream.Stream;

import static com.github.dakusui.actionunit.core.ActionSupport.named;
import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;

/**
 * A base builder class to construct an action that runs a command line program.
 *
 * @param <C> A class refers to itself.
 * @see com.github.dakusui.actionunit.actions.cmd.unix.Cmd
 */
public abstract class Commander<C extends Commander<C>> implements Cloneable {
  private static final Logger LOGGER = LoggerFactory.getLogger(Commander.class);
  CommandLineComposer.Builder commandLineComposerBuilder;
  private final Function<ContextVariable[], IntFunction<String>> parameterPlaceHolderFactory;

  private RetryOption                retryOption;
  private Supplier<Consumer<String>> downstreamConsumerFactory;
  private Supplier<Checker>          checkerFactory;
  private Stream<String>             stdin;

  private ShellManager shellManager;

  private       File                               cwd         = null;
  private final Map<String, String>                envvars;
  private       String                             description = null;
  private       String                             host;


  protected Commander(CommanderConfig config) {
    this.parameterPlaceHolderFactory = config.variablePlaceHolderFormatter();
    this.envvars = new LinkedHashMap<>();
    this.stdin(Stream.empty())
        .retryOption(config.retryOption())
        .shellManager(config.shellManager())
        .host("localhost")
        .checker(config.checker())
        .downstreamConsumer(LOGGER::trace);
  }

  @SuppressWarnings("unchecked")
  public C describe(String description) {
    this.description = description;
    return (C) this;
  }

  @SuppressWarnings("unchecked")
  public C host(String host) {
    this.host = requireNonNull(host);
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
  public C shellManager(ShellManager shellManager) {
    this.shellManager = requireNonNull(shellManager);
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
    Action action = CommanderUtils.createAction(this);
    return this.description != null ?
        named(this.description, action) :
        action;
  }

  public Action build() {
    return toAction();
  }

  public Action $() {
    return build();
  }

  /**
   * A building method that returns {@link StreamGenerator} object.
   *
   * @return A stream generator object.
   */
  public Function<Context, Stream<String>> toStreamGenerator() {
    return CommanderUtils.createStreamGenerator(this);
  }

  public Consumer<Context> toContextConsumer() {
    return CommanderUtils.createContextConsumer(this);
  }

  public Predicate<Context> toContextPredicate() {
    return CommanderUtils.createContextPredicate(this);
  }

  public Function<Context, String> toContextFunction() {
    return CommanderUtils.createContextFunction(this);
  }

  /**
   * A short-hand method to execute a command directly.
   *
   * @return data stream from the executed command.
   */
  public Stream<String> run() {
    return run(emptyMap());
  }

  /**
   * A short-hand method to execute a command directly.
   *
   * @param variables Variables to be set to the context.
   * @return data stream from the executed command.
   */
  public Stream<String> run(Map<String, Object> variables) {
    Context context = Context.create();
    requireNonNull(variables).forEach(context::assignTo);
    return toStreamGenerator().apply(context);
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
  public C add(Function<Context, String> target) {
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

  public C append(Function<Context, String> func) {
    return append(func, false);
  }

  public C appendq(Function<Context, String> func) {
    return append(func, true);
  }

  @SuppressWarnings("unchecked")
  public C append(Function<Context, String> func, boolean b) {
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

  public C appendVariable(ContextVariable variableName) {
    return appendVariable(variableName, false);
  }

  public C appendQuotedVariable(ContextVariable variableName) {
    return appendVariable(variableName, true);
  }

  @SuppressWarnings("unchecked")
  public C appendVariable(ContextVariable variableName, boolean b) {
    commandLineComposerBuilderIfSet().appendVariable(variableName, b);
    return (C) this;
  }

  @SuppressWarnings("unchecked")
  public C declareVariable(ContextVariable variableName) {
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

  public ShellManager shellManager() {
    return this.shellManager;
  }

  public Map<String, String> envvars() {
    return Collections.unmodifiableMap(this.envvars);
  }

  public Optional<File> cwd() {
    return Optional.ofNullable(this.cwd);
  }

  @Override
  public String toString() {
    return format(
        "%s:Shell:(%s), CommandLine(%s)",
        this.getClass().getSimpleName(),
        shellManager(),
        commandLineComposerBuilder);
  }

  @SuppressWarnings("unchecked")
  public C commandName(String command) {
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

  ContextVariable[] variables() {
    return commandLineComposerBuilderIfSet().knownVariables();
  }

  Optional<CommandLineComposer.Builder> commandLineComposerBuilder() {
    return Optional.ofNullable(commandLineComposerBuilder);
  }

  private Function<ContextVariable[], IntFunction<String>> parameterPlaceHolderFactory() {
    return this.parameterPlaceHolderFactory;
  }

  public String host() {
    return this.host;
  }
}
