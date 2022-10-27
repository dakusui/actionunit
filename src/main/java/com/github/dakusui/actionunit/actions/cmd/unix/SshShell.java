package com.github.dakusui.actionunit.actions.cmd.unix;

import com.github.dakusui.processstreamer.core.process.Shell;

import java.util.LinkedList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public interface SshShell extends Shell {
  class Impl implements SshShell {
    private final String       program;
    private final List<String> options;

    Impl(String program, List<String> options) {
      this.program = program;
      this.options = options;
    }

    @Override
    public String program() {
      return program;
    }

    @Override
    public List<String> options() {
      return options;
    }

    @Override
    public String toString() {
      return String.format("%s %s", program, String.join(" ", options));
    }
  }

  class Builder {
    /**
     * A name of ssh program. Usually just `ssh`.
     */
    private       String program;
    private       String user;
    private final String host;

    /**
     * Holds options specific to {@code ssh} command. That is, options supported
     * by {@code ssh} but not by {@code scp} should be stored in this field.
     */
    private final SshOptions.Builder optionsBuilder;

    public Builder(String host) {
      this(host, new SshOptions.Builder());
    }

    public Builder(String host, SshOptions.Builder optionsBuilder) {
      this.program("ssh");
      this.host = requireNonNull(host);
      this.optionsBuilder = requireNonNull(optionsBuilder);
    }

    public Builder program(String program) {
      this.program = requireNonNull(program);
      return this;
    }

    public Builder user(String user) {
      this.user = requireNonNull(user);
      return this;
    }

    public Builder enableAuthAgentConnectionForwarding() {
      this.optionsBuilder.addSshOption("-A");
      return this;
    }

    public Shell build() {
      List<String> options = new LinkedList<String>() {{
        this.addAll(Builder.this.optionsBuilder.build().options(SshOptions.Formatter.forSsh()));
        this.add(
            user != null ?
                String.format("%s@%s", user, host) :
                host);
      }};
      return new Impl(program, options);
    }
  }
}
