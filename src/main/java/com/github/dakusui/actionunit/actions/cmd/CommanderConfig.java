package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.actionunit.actions.ContextVariable;
import com.github.dakusui.actionunit.actions.RetryOption;
import com.github.dakusui.actionunit.actions.cmd.unix.*;
import com.github.dakusui.processstreamer.core.process.ProcessStreamer;
import com.github.dakusui.processstreamer.core.process.Shell;

import java.util.function.Function;
import java.util.function.IntFunction;

import static com.github.dakusui.processstreamer.core.process.ProcessStreamer.Checker.createCheckerForExitCode;
import static java.util.Objects.requireNonNull;

public interface CommanderConfig {
  Function<ContextVariable[], IntFunction<String>> DEFAULT_PLACE_HOLDER_FORMATTER = variables -> i -> String.format("{{%s}}", i);
  Function<ContextVariable[], IntFunction<String>> PLACE_HOLDER_FORMATTER_BY_NAME = variables -> i -> String.format("{{%s}}", variables[i]);

  CommanderConfig                                  DEFAULT                        = CommanderConfig.builder().placeHolderFormatter(DEFAULT_PLACE_HOLDER_FORMATTER).build();

  Shell shell();

  SshOptions sshOptions();

  RetryOption retryOption();

  Function<ContextVariable[], IntFunction<String>> variablePlaceHolderFormatter();

  ProcessStreamer.Checker checker();

  static Builder builder() {
    return new Builder();
  }

  class Impl implements CommanderConfig {
    final SshOptions              sshOptions;
    final Shell                   shell;
    final RetryOption             retryOption;
    final ProcessStreamer.Checker processStreamerChecker;

    final Function<ContextVariable[], IntFunction<String>> placeHolderFormatter;

    public Impl(
        SshOptions sshOptions,
        Shell shell,
        RetryOption retryOption,
        ProcessStreamer.Checker processStreamerChecker,
        Function<ContextVariable[], IntFunction<String>> placeHolderFormatter) {
      this.sshOptions = requireNonNull(sshOptions);
      this.shell = requireNonNull(shell);
      this.retryOption = requireNonNull(retryOption);
      this.processStreamerChecker = requireNonNull(processStreamerChecker);
      this.placeHolderFormatter = requireNonNull(placeHolderFormatter);
    }

    @Override
    public Shell shell() {
      return this.shell;
    }

    @Override
    public SshOptions sshOptions() {
      return this.sshOptions;
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
  }

  class Builder {
    SshOptions                                       sshOptions;
    Shell                                            shell;
    RetryOption                                      retryOption;
    ProcessStreamer.Checker                          processStreamerChecker;
    Function<ContextVariable[], IntFunction<String>> placeHolderFormatter;

    public Builder() {
      this.sshOptions(new SshOptions.Builder()
              .disableStrictHostkeyChecking()
              .disablePasswordAuthentication()
              .build())
          .processStreamerChecker(createCheckerForExitCode(0))
          .shell(Shell.LOCAL_SHELL)
          .retryOption(RetryOption.none())
          .placeHolderFormatter(DEFAULT_PLACE_HOLDER_FORMATTER);
    }

    public Builder sshOptions(SshOptions sshOptions) {
      this.sshOptions = requireNonNull(sshOptions);
      return this;
    }

    public Builder shell(Shell shell) {
      this.shell = requireNonNull(shell);
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
      return new CommanderConfig.Impl(sshOptions, shell, retryOption, processStreamerChecker, placeHolderFormatter);
    }
  }
}
