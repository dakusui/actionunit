package com.github.dakusui.actionunit.actions.cmd.compat;

import com.github.dakusui.actionunit.actions.cmd.BaseCommander;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.processstreamer.core.process.Shell;

import java.io.File;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public interface Cmd {
  static Builder builder() {
    return new Builder();
  }

  Stream<String> stream();

  Cmd readFrom(Stream<String> stream);

  class Builder {
    private Shell               shell;
    private Consumer<String>    stdoutConsumer;
    private Consumer<String>    stderrConsumer;
    private File                cwd;
    private Map<String, String> env;
    private Supplier<String>    commandLineSupplier;

    Builder with(Shell shell) {
      this.shell = requireNonNull(shell);
      return this;
    }

    public Builder consumeStdout(Consumer<String> stdoutConsumer) {
      this.stdoutConsumer = requireNonNull(stdoutConsumer);
      return this;
    }

    public Builder consumeStderr(Consumer<String> stderrConsumer) {
      this.stderrConsumer = requireNonNull(stderrConsumer);
      return this;
    }

    public Builder cwd(File cwd) {
      this.cwd = requireNonNull(cwd);
      return this;
    }

    public File cwd() {
      return this.cwd;
    }

    public Builder env(Map<String, String> env) {
      this.env = requireNonNull(env);
      return this;
    }

    public Builder command(Supplier<String> commandLineSupplier) {
      this.commandLineSupplier = requireNonNull(commandLineSupplier);
      return this;
    }

    public Cmd build() {
      return new Cmd() {
        BaseCommander commander = new BaseCommander(shell);

        @Override
        public Stream<String> stream() {
          BaseCommander commander = this.commander;
          if (cwd != null)
            commander = (BaseCommander) commander.cwd(cwd);
          for (String key : env.keySet())
            commander = (BaseCommander) commander.env(key, env.get(key));
          return ((Stream<String>) commander.command(commandLineSupplier.get())
              .toStreamGenerator()
              .apply(Context.create()))
              .peek(stdoutConsumer);
        }

        @Override
        public Cmd readFrom(Stream<String> stream) {
          commander.stdin(stream);
          return this;
        }
      };
    }
  }
}
