package com.github.dakusui.actionunit.actions.cmd;


import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.DataSupplier;
import com.github.dakusui.actionunit.utils.Checks;
import com.github.dakusui.cmd.Cmd;
import com.github.dakusui.cmd.Shell;
import com.github.dakusui.cmd.exceptions.UnexpectedExitValueException;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.dakusui.actionunit.actions.cmd.CommanderUtils.quoteWithSingleQuotesForShell;
import static com.github.dakusui.actionunit.core.ActionSupport.*;
import static com.github.dakusui.actionunit.utils.Checks.*;
import static java.util.Objects.requireNonNull;

/**
 * COMMAND action buildER class for ActionUnit style.
 *
 * @param <B> The class you implement by extending this class.
 */
public abstract class Commander<B extends Commander<B>> extends Action.Builder<Action> implements Cloneable {
  private final int                  summaryLength;
  private       DataSupplier<String> stdin;

  private static final Consumer<String> DEFAULT_STDOUT_CONSUMER = System.out::println;
  private static final Consumer<String> DEFAULT_STDERR_CONSUMER = System.err::println;
  List<Function<Context, String>> options;
  private       Cmd.Builder                cmdBuilder;
  private       int                        numRetries;
  private       String                     description;
  private       long                       retryIntervalDuration;
  private       TimeUnit                   retryIntervalTimeUnit;
  private       TimeUnit                   timeOutTimeUnit;
  private       long                       timeOutDuration;
  private       Class<? extends Throwable> retryOn = UnexpectedExitValueException.class;
  private       File                       cwd     = null;
  private final Map<String, String>        env     = new LinkedHashMap<>();


  /**
   * This is a helper method to create a {@code Commander} object for a given
   * {@code command} without defining a custom class.
   *
   * @param command A command for which the returned builder works.
   * @return A new {@code Commander} object.
   */
  @SuppressWarnings("unchecked")
  public static Commander<?> commander(String command) {
    return new Commander() {
      @Override
      protected String program() {
        return command;
      }
    };
  }

  /**
   * Creates an instance of this class.
   */
  public Commander() {
    this.summaryLength = 60;
    this.options = new LinkedList<>();
    this.cmdBuilder = Cmd.builder()
        .with(new Bash())
        .consumeStdout(DEFAULT_STDOUT_CONSUMER)
        .consumeStderr(DEFAULT_STDERR_CONSUMER);
    this.disconnectStdin().disableTimeout();
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

  @SuppressWarnings("unchecked")
  public B describe(String description) {
    this.description = requireNonNull(description);
    return (B) this;
  }

  @SuppressWarnings("unchecked")
  public B stdin(DataSupplier<String> stdin) {
    this.stdin = requireNonNull(stdin);
    return (B) this;
  }

  public B disconnectStdin() {
    return stdin(Stream::empty);
  }

  /**
   * Exposes a builder of {@code Cmd} object.
   *
   * @return An internal builder for {@code Cmd} object.
   */
  public Cmd.Builder cmdBuilder() {
    return this.cmdBuilder;
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

  /**
   * This method returns a stream for standard output of a command built by
   * this builder.
   * <p>
   * And this does not create any action. This method is meant to reuse a
   * {@code Commander} object create for creating an action to other purposes
   * such as a source of data used in a {@code ForEach} action structure.
   * {@code toStream(Context)} calls {@code composeCmd(Context)} to create a {@code Cmd}
   * object that creates a stream to be returned.
   *
   * @param context A context object from which a {@code cmd} object is created.
   * @return A stream for standard output of the command.
   * @see Commander#composeCmd(Context)
   */
  public Stream<String> toStream(Context context) {
    return composeCmd(context).stream();
  }

  /**
   * This method returns an iterator for standard output of a command built by
   * this builder.
   *
   * @param context A context object from which {@code cmd} object is created.
   * @return An iterator for standard output of the command.n
   * @see Commander#toStream(Context)
   */
  public Iterable<String> toIterable(Context context) {
    return () -> toStream(context).iterator();
  }

  @Override
  public String toString() {
    return Objects.toString(this.description);
  }

  private Action readFrom(DataSupplier<String> in) {
    return numRetries > 0 ?
        retry(
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

  private String description() {
    return CommanderUtils.summarize(
        this.description != null
            ? this.description
            : "(commander)",
        this.summaryLength
    );
  }

  /**
   * Add {@code option} quoting with single quotes "'".
   *
   * @param option an option to be added with quotes.
   * @return This object
   */
  public B addq(String option) {
    requireNonNull(option);
    return this.addq(new Function<Context, String>() {
      @Override
      public String apply(Context context) {
        return option;
      }

      @Override
      public String toString() {
        return option;
      }
    });
  }

  @SuppressWarnings("unchecked")
  public B addq(Function<Context, String> option) {
    requireNonNull(option);
    options.add(new Function<Context, String>() {
      @Override
      public String apply(Context context) {
        return quoteWithSingleQuotesForShell(option.apply(context));
      }

      @Override
      public String toString() {
        return String.format("<%s>", option);
      }
    });
    return (B) this;
  }

  @SuppressWarnings("unchecked")
  public B add(Function<Context, String> option) {
    options.add(requireNonNull(option));
    return (B) this;
  }

  @SuppressWarnings("unchecked")
  public B add(String option) {
    requireNonNull(option);
    options.add(new Function<Context, String>() {
      @Override
      public String apply(Context context) {
        return option;
      }

      @Override
      public String toString() {
        return option;
      }
    });
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

  /**
   * Sets current working directory of a command built by this object to a
   * given value.
   *
   * @param cwd Current working directory for a command built by this object.
   * @return this object
   */
  @SuppressWarnings("unchecked")
  public B cwd(File cwd) {
    requireArgument(File::isDirectory, requireNonNull(cwd));
    this.cwd = cwd;
    this.cmdBuilder.cwd(cwd);
    return (B) this;
  }

  /**
   * Returns a current working directory set to this object
   *
   * @return A current working directory set to this object.
   */
  public File cwd() {
    if (this.cwd == null)
      return new File(System.getProperty("user.dir"));
    return this.cwd;
  }

  /**
   * Sets an environment variable used by a command created by this object.
   *
   * @param envvar A name of environment variable.
   * @param value  A value for an environment variable {@code envvar}.
   * @return This object.
   */
  @SuppressWarnings("unchecked")
  public B env(String envvar, String value) {
    this.env.put(requireNonNull(envvar), requireNonNull(value));
    return (B) this;
  }

  /**
   * Creates a {@code Cmd} object based on properties this object holds.
   *
   * @param context A context object from which {@code Cmd} object to be returned
   *                is created.
   * @return A created {@code Cmd} object.
   */
  public Cmd toCmd(Context context) {
    return composeCmd(context);
  }

  /**
   * Get full path to a command for which this builder object works.
   *
   * @return full path of command
   */
  protected abstract String program();

  private Action timeOutIfNecessary(Action action) {
    return this.timeOutDuration > 0
        ? timeout(action).in(this.timeOutDuration, this.timeOutTimeUnit)
        : action;
  }

  private Action composeAction(DataSupplier<String> in) {
    return named(
        description(),
        leaf(
            context -> composeCmd(context).readFrom(requireNonNull(in.get())).stream().forEach(s -> {
            })
        )
    );
  }

  private Cmd composeCmd(Context context) {
    cmdBuilder.env(this.env);
    cmdBuilder.command(new Supplier<String>() {
      @Override
      public String get() {
        return String.format(
            "%s %s",
            Commander.this.program(),
            Commander.this.formatOptions(option -> option.apply(context))
        );
      }

      @Override
      public String toString() {
        return String.format(
            "%s %s",
            Commander.this.program(),
            Commander.this.formatOptions(stringSupplier -> {
              String ret = Objects.toString(stringSupplier);
              return ret.contains("$$Lambda") ?
                  "(?)" :
                  ret;
            })
        );
      }
    });
    return cmdBuilder.build();
  }

  private String formatOptions(Function<Function<Context, String>, String> formatter) {
    return this.options.stream()
        .map(formatter)
        .collect(Collectors.joining(" "));
  }

  /**
   * A shell with which a command built by {@code Commander} object is executed
   * by default.
   */
  public static class Bash implements Shell {
    /**
     * Returns a path to a bash program.
     *
     * @return A path to a bash program.
     */
    @Override
    public String program() {
      return "/bin/bash";
    }

    /**
     * Returns an option used to execute target command. For this class, it
     * is defined {@code ["-c"]} based on bash's behaviour.
     *
     * @return Returns a command line option passed to shell.
     */
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
}
