package com.github.dakusui.actionunit.extras.cmd;


import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.helpers.Checks;
import com.github.dakusui.actionunit.visitors.reporting.ReportingActionPerformer;
import com.github.dakusui.cmd.Cmd;
import com.github.dakusui.cmd.Shell;
import com.github.dakusui.cmd.exceptions.UnexpectedExitValueException;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.dakusui.actionunit.helpers.Checks.*;
import static java.util.Objects.requireNonNull;

/**
 * COMMAND action buildER class for ActionUnit style.
 *
 * @param <B> The class itself you implement by extending this class.
 */
public abstract class Commander<B extends Commander<B>> implements Cloneable {
  private Stream<String> stdin = null;

  private static final Consumer<String> DEFAULT_STDOUT_CONSUMER = System.out::println;
  private static final Consumer<String> DEFAULT_STDERR_CONSUMER = System.err::println;
  @SuppressWarnings("WeakerAccess")
  List<String> options;
  private       Cmd.Builder                cmdBuilder;
  private final Context                    context;
  private       int                        numRetries;
  private       String                     description;
  private       long                       retryIntervalDuration;
  private       TimeUnit                   retryIntervalTimeUnit;
  private       TimeUnit                   timeOutTimeUnit;
  private       long                       timeOutDuration;
  private       Class<? extends Throwable> retryOn = UnexpectedExitValueException.class;
  private       File                       cwd     = null;

  public Commander(Context context) {
    this.context = requireNonNull(context);
    this.options = new LinkedList<>();
    this.cmdBuilder = Cmd.builder()
        .with(new Bash())
        .consumeStdout(DEFAULT_STDOUT_CONSUMER)
        .consumeStderr(DEFAULT_STDERR_CONSUMER);
    this.consumeStderrWith(DEFAULT_STDERR_CONSUMER).disconnectStdin().disableTimeout();
  }

  @SuppressWarnings("unchecked")
  public B clone() {
    try {
      B ret = ((B) super.clone());
      ret.options = new LinkedList<>(ret.options);
      return ret;
    } catch (CloneNotSupportedException e) {
      throw impossibleLineReached(e.getMessage(), e);
    }
  }

  public static String summarize(String commandLine, int length) {
    Checks.requireArgument(l -> l > 3, length);
    return requireNonNull(commandLine).length() < length ?
        replaceNewLines(commandLine) :
        replaceNewLines(commandLine).substring(0, length - 3) + "...";
  }

  private static String escapeSingleQuotesForShell(String s) {
    return requireNonNull(s).replaceAll("('+)", "'\"$1\"'");
  }

  private static String replaceNewLines(String s) {
    return s.replaceAll("\n", " ");
  }

  @SuppressWarnings("unchecked")
  public B describe(String description) {
    this.description = requireNonNull(description);
    return (B) this;
  }

  @SuppressWarnings("unchecked")
  public B stdin(Stream<String> stdin) {
    this.stdin = stdin;
    return (B) this;
  }

  public B disconnectStdin() {
    return stdin(null);
  }

  @SuppressWarnings("unchecked")
  public B consumeStdoutWith(Consumer<String> c) {
    this.cmdBuilder.consumeStdout(c);
    return (B) this;
  }

  @SuppressWarnings("unchecked")
  public B consumeStderrWith(Consumer<String> c) {
    this.cmdBuilder.consumeStderr(c);
    return (B) this;
  }

  @SuppressWarnings("unchecked")
  public B transformStdoutWith(Function<Stream<String>, Stream<String>> transformer) {
    cmdBuilder.transformStdout(transformer);
    return (B) this;
  }

  @SuppressWarnings("unchecked")
  public B transformStderrWith(Function<Stream<String>, Stream<String>> transformer) {
    cmdBuilder.transformStderr(transformer);
    return (B) this;
  }

  @SuppressWarnings("unchecked")
  public B transformInputWith(Function<Stream<String>, Stream<String>> transformer) {
    cmdBuilder.transformInput(transformer);
    return (B) this;
  }

  @SuppressWarnings("unchecked")
  public B cmd(Consumer<? super Cmd.Builder> b) {
    requireNonNull(b).accept(this.cmdBuilder);
    return (B) this;
  }


  @SuppressWarnings("unchecked")
  public B retries(int times) {
    this.numRetries = Checks.requireArgument(v -> v >= 0, times);
    return (B) this;
  }

  @SuppressWarnings("unchecked")
  public B retryOn(Class<? extends Throwable> retryOn) {
    this.retryOn = requireNonNull(retryOn);
    return (B) this;
  }

  @SuppressWarnings("unchecked")
  public B interval(long duration, TimeUnit timeUnit) {
    checkArgument(duration > 0);
    requireNonNull(timeUnit);
    this.retryIntervalDuration = duration;
    this.retryIntervalTimeUnit = timeUnit;
    return (B) this;
  }

  @SuppressWarnings("unchecked")
  public B timeoutIn(long duration, TimeUnit timeUnit) {
    checkArgument(duration > 0);
    requireNonNull(timeUnit);
    this.timeOutDuration = duration;
    this.timeOutTimeUnit = timeUnit;
    return (B) this;
  }

  @SuppressWarnings("unchecked")
  public B disableTimeout() {
    this.timeOutDuration = 0;
    return (B) this;
  }

  public Action build() {
    return readFrom(this.stdin);
  }

  private Action readFrom(Stream<String> in) {
    return numRetries > 0 ?
        this.context.retry(
            this.timeOutIfNecessary(composeAction(in))
        ).on(
            this.retryOn
        ).times(
            this.numRetries
        ).withIntervalOf(
            this.retryIntervalDuration, this.retryIntervalTimeUnit
        ).build() :
        timeOutIfNecessary(composeAction(in));
  }

  public Stream<String> toStream() {
    return composeCmd().stream();
  }

  public Iterable<String> toIterable() {
    return () -> toStream().iterator();
  }

  private Action timeOutIfNecessary(Action action) {
    return this.timeOutDuration > 0 ?
        this.context.timeout(action).in(this.timeOutDuration, this.timeOutTimeUnit) :
        action;
  }

  private Action composeAction(Stream<String> in) {
    Cmd cmd = composeCmd();
    return this.context.simple(
        description().orElse(summarize(cmd.getCommand(), 60)),
        () -> {
          Cmd internalCmd = cmd;
          if (!cmd.getState().equals(Cmd.State.PREPARING)) {
            // re-build is required to reset status in Cmd. this action is possible to be repeated when retry.
            internalCmd = composeCmd();
          }
          if (in != null)
            internalCmd.readFrom(in);
          internalCmd.stream().forEach(s -> {
          });
        }
    );
  }

  @Override
  public String toString() {
    return Objects.toString(this.description);
  }

  private Optional<String> description() {
    return Optional.ofNullable(this.description);
  }

  public static void run(Action action) {
    new ReportingActionPerformer.Builder(action).build().performAndReport();
  }

  public static Context ROOT_CONTEXT = new Context.Impl();

  /**
   * Add {@code option} quoting with single quotes "'".
   *
   * @param option an option to be added with quotes.
   * @return This object
   */
  public B addq(String option) {
    return this.add(quoteWithSingleQuotesForShell(option));
  }

  @SuppressWarnings("unchecked")
  public B add(String option) {
    options.add(requireNonNull(option));
    return (B) this;
  }

  public B add(CommanderOption option) {
    return add(option, false);
  }

  @SuppressWarnings("unchecked")
  public B add(CommanderOption option, boolean longFormat) {
    return (B) requireNonNull(option).addTo(this, longFormat);
  }

  public B add(CommanderOption option, String value) {
    return add(option, value, false);
  }

  @SuppressWarnings("unchecked")
  public B add(CommanderOption option, String value, boolean longFormat) {
    return (B) requireNonNull(option).addTo(this, value, longFormat);
  }

  @SuppressWarnings("unchecked")
  public B cwd(File cwd) {
    requireArgument(File::isDirectory, requireNonNull(cwd));
    this.cwd = cwd;
    cmdBuilder.cwd(cwd);
    return (B) this;
  }

  @SuppressWarnings("unchecked")
  public B env(Map<String, String> env) {
    cmdBuilder.env(env);
    return (B) this;
  }

  public Cmd toCmd() {
    return composeCmd();
  }

  private Cmd composeCmd() {
    cmdBuilder.command(String.format(
        "%s %s",
        commandPath(),
        String.join(" ", this.options())
    ));
    return cmdBuilder.build();
  }

  protected List<String> options() {
    return Collections.unmodifiableList(this.options);
  }

  /**
   * Get full path of command
   *
   * @return full path of command
   */
  protected abstract String commandPath();

  protected File cwd() {
    requireState(v -> v != null, this.cwd);
    return this.cwd;
  }

  public static class Bash implements Shell {
    @Override
    public String program() {
      return "bash";
    }

    @Override
    public List<String> options() {
      return new ArrayList<String>() {{
        add("-c");
      }};
    }

    @Override
    public String toString() {
      return format();
    }
  }

  public static String quoteWithSingleQuotesForShell(String s) {
    return String.format("'%s'", escapeSingleQuotesForShell(s));
  }
}
