package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.actionunit.actions.ContextVariable;
import com.github.dakusui.actionunit.actions.RetryOption;
import com.github.dakusui.actionunit.actions.cmd.unix.SshOptions;
import com.github.dakusui.processstreamer.core.process.ProcessStreamer;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;

import static com.github.dakusui.actionunit.actions.cmd.unix.SshOptions.emptySshOptions;
import static com.github.dakusui.processstreamer.core.process.ProcessStreamer.Checker.createCheckerForExitCode;
import static java.util.Objects.requireNonNull;

public interface CommanderConfig {
  Function<ContextVariable[], IntFunction<String>> DEFAULT_PLACE_HOLDER_FORMATTER = variables -> i -> String.format("{{%s}}", i);
  Function<ContextVariable[], IntFunction<String>> PLACE_HOLDER_FORMATTER_BY_NAME = variables -> i -> String.format("{{%s}}", variables[i].variableName());

  CommanderConfig DEFAULT = CommanderConfig.builder()
      .shellManager(ShellManager.createShellManager(h -> new SshOptions.Builder()
          .disableStrictHostkeyChecking()
          .disablePasswordAuthentication()
          .build()))
      .programNameResolver(ProgramNameResolver.createForUnix())
      .placeHolderFormatter(DEFAULT_PLACE_HOLDER_FORMATTER)
      .build();

  ShellManager shellManager();

  /**
   * Returns a function that resolves an appropriate {@link SshOptions} object from a given host name.
   *
   * @return An ssh options resolver function.
   */
  Function<String, SshOptions> sshOptionsResolver();

  /**
   * Note that the returned objecc is only used by {@link Commander#toAction()} method,
   * not by other builder methods such as {@link Commander#toContextPredicate()}, {@link Commander#toContextFunction()}, etc.
   *
   * @return A retry option object.
   * @see Commander
   */
  RetryOption retryOption();

  Function<ContextVariable[], IntFunction<String>> variablePlaceHolderFormatter();

  ProcessStreamer.Checker checker();

  static Builder builder() {
    return new Builder();
  }

  BiFunction<String, String, String> programNameResolver();

  class Impl implements CommanderConfig {
    final ShellManager            shellManager;
    final RetryOption             retryOption;
    final ProcessStreamer.Checker processStreamerChecker;

    final Function<ContextVariable[], IntFunction<String>> placeHolderFormatter;

    final Function<String, SshOptions>       sshOptionsResolver;
    final BiFunction<String, String, String> programNameResolver;

    public Impl(
        ShellManager shellManager,
        BiFunction<String, String, String> programNameResolver,
        Function<String, SshOptions> sshOptionsResolver,
        RetryOption retryOption,
        ProcessStreamer.Checker processStreamerChecker,
        Function<ContextVariable[], IntFunction<String>> placeHolderFormatter) {
      this.shellManager = requireNonNull(shellManager);
      this.programNameResolver = requireNonNull(programNameResolver);
      this.retryOption = requireNonNull(retryOption);
      this.processStreamerChecker = requireNonNull(processStreamerChecker);
      this.placeHolderFormatter = requireNonNull(placeHolderFormatter);
      this.sshOptionsResolver = requireNonNull(sshOptionsResolver);
    }

    @Override
    public ShellManager shellManager() {
      return this.shellManager;
    }

    @Override
    public Function<String, SshOptions> sshOptionsResolver() {
      return this.sshOptionsResolver;
    }

    @Override
    public RetryOption retryOption() {
      return this.retryOption;
    }

    @Override
    public Function<ContextVariable[], IntFunction<String>> variablePlaceHolderFormatter() {
      return this.placeHolderFormatter;
    }

    @Override
    public ProcessStreamer.Checker checker() {
      return this.processStreamerChecker;
    }

    @Override
    public BiFunction<String, String, String> programNameResolver() {
      return this.programNameResolver;
    }
  }

  class Builder {
    ShellManager shellManager;

    BiFunction<String, String, String> programNameResolver;

    Function<String, SshOptions> sshOptionsResolver;

    RetryOption                                      retryOption;
    ProcessStreamer.Checker                          processStreamerChecker;
    Function<ContextVariable[], IntFunction<String>> placeHolderFormatter;

    public Builder() {
      this.processStreamerChecker(createCheckerForExitCode(0))
          .shellManager(ShellManager.createShellManager())
          .programNameResolver(ProgramNameResolver.createForUnix())
          .retryOption(RetryOption.none())
          .sshOptionsResolver(h -> emptySshOptions())
          .placeHolderFormatter(DEFAULT_PLACE_HOLDER_FORMATTER);
    }

    private Builder programNameResolver(BiFunction<String, String, String> programNameResolver) {
      this.programNameResolver = requireNonNull(programNameResolver);
      return this;
    }

    public Builder shellManager(ShellManager shellManager) {
      this.shellManager = requireNonNull(shellManager);
      return this.sshOptionsResolver(h -> this.shellManager.sshOptionsFor(h));
    }

    public Builder sshOptionsResolver(Function<String, SshOptions> sshOptionsResolver) {
      this.sshOptionsResolver = requireNonNull(sshOptionsResolver);
      return this;
    }

    public Builder retryOption(RetryOption retryOption) {
      this.retryOption = retryOption;
      return this;
    }

    public Builder processStreamerChecker(ProcessStreamer.Checker processStreamerChecker) {
      this.processStreamerChecker = requireNonNull(processStreamerChecker);
      return this;
    }

    public Builder placeHolderFormatter(Function<ContextVariable[], IntFunction<String>> placeHolderFormatter) {
      this.placeHolderFormatter = placeHolderFormatter;
      return this;
    }

    public CommanderConfig build() {
      return new CommanderConfig.Impl(shellManager, programNameResolver, sshOptionsResolver, retryOption, processStreamerChecker, placeHolderFormatter);
    }
  }
}
