package com.github.dakusui.actionunit.extras.cmd;


import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.helpers.Checks;
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

import static com.github.dakusui.actionunit.extras.cmd.CommanderUtils.quoteWithSingleQuotesForShell;
import static com.github.dakusui.actionunit.helpers.Checks.*;
import static java.util.Objects.requireNonNull;

/**
 * COMMAND action buildER class for ActionUnit style.
 *
 * @param <B> The class itself you implement by extending this class.
 */
public abstract class Commander<B extends Commander<B>> implements Cloneable {
  private final int            summaryLength;
  private       Stream<String> stdin = null;

  private static final Consumer<String> DEFAULT_STDOUT_CONSUMER = System.out::println;
  private static final Consumer<String> DEFAULT_STDERR_CONSUMER = System.err::println;
  List<Supplier<String>> options;
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
  private final Map<String, String>        env     = new LinkedHashMap<>();


  /**
   * This is a helper method to create a {@code Commander} object for a given
   * {@code command} without defining a custom class.
   *
   * @param context A context to create an action.
   * @param command A command for which the returned builder works.
   * @return A new {@code Commander} object.
   */
  @SuppressWarnings("unchecked")
  public static Commander<?> commander(Context context, String command) {
    return new Commander(context) {
      @Override
      protected String program() {
        return command;
      }
    };
  }

  /**
   * Creates an instance of this class.
   *
   * @param context A context from which this builder object creates an action.
   */
  public Commander(Context context) {
    this.context = requireNonNull(context);
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
  public B stdin(Stream<String> stdin) {
    this.stdin = stdin;
    return (B) this;
  }

  public B disconnectStdin() {
    return stdin(null);
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
   *
   * @return A stream for standard output of the command.
   */
  public Stream<String> toStream() {
    return composeCmd().stream();
  }

  /**
   * This method returns an iterator for standard output of a command built by
   * this builder.
   *
   * @return An iterator for standard output of the command.
   * @see Commander#toStream()
   */
  public Iterable<String> toIterable() {
    return () -> toStream().iterator();
  }

  @Override
  public String toString() {
    return Objects.toString(this.description);
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

  private Optional<String> description() {
    return Optional.ofNullable(this.description);
  }

  /**
   * Add {@code option} quoting with single quotes "'".
   *
   * @param option an option to be added with quotes.
   * @return This object
   */
  public B addq(String option) {
    requireNonNull(option);
    return this.addq(new Supplier<String>() {
      @Override
      public String get() {
        return option;
      }

      @Override
      public String toString() {
        return option;
      }
    });
  }

  @SuppressWarnings("unchecked")
  public B addq(Supplier<String> option) {
    requireNonNull(option);
    options.add(new Supplier<String>() {
      @Override
      public String get() {
        return quoteWithSingleQuotesForShell(option.get());
      }

      @Override
      public String toString() {
        return option.toString();
      }
    });
    return (B) this;
  }

  @SuppressWarnings("unchecked")
  public B add(Supplier<String> option) {
    options.add(requireNonNull(option));
    return (B) this;
  }

  @SuppressWarnings("unchecked")
  public B add(String option) {
    requireNonNull(option);
    options.add(new Supplier<String>() {
      @Override
      public String get() {
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
   * @return A created {@code Cmd} object.
   */
  public Cmd toCmd() {
    return composeCmd();
  }

  /**
   * Get full path to a command for which this builder object works.
   *
   * @return full path of command
   */
  protected abstract String program();

  private Action timeOutIfNecessary(Action action) {
    return this.timeOutDuration > 0 ?
        this.context.timeout(action).in(this.timeOutDuration, this.timeOutTimeUnit) :
        action;
  }

  private Action composeAction(Stream<String> in) {
    Cmd cmd = composeCmd();
    return this.context.simple(
        description().orElse(CommanderUtils.summarize(cmd.getCommand().toString(), summaryLength)),
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

  private Cmd composeCmd() {
    cmdBuilder.env(this.env);
    cmdBuilder.command(new Supplier<String>() {
      @Override
      public String get() {
        return String.format(
            "%s %s",
            Commander.this.program(),
            Commander.this.formatOptions(Supplier::get)
        );
      }

      @Override
      public String toString() {
        return String.format(
            "%s %s",
            Commander.this.program(),
            Commander.this.formatOptions(new Function<Supplier<String>, String>() {
              @Override
              public String apply(Supplier<String> stringSupplier) {
                String ret = Objects.toString(stringSupplier);
                return ret.contains("$$Lambda") ?
                    "(?)" :
                    ret;
              }
            })
        );
      }
    });
    return cmdBuilder.build();
  }

  private String formatOptions(Function<Supplier<String>, String> formatter) {
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
