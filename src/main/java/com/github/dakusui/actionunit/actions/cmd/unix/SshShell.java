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
    private       String       program;
    private       String       user;
    private final String       host;
    /**
     * Holds options specific to {@code ssh} command. That is, options supported
     * by {@code ssh} but not by {@code scp} should be stored in this field.
     */
    private final List<String> options;
    /**
     * Holds options common to both {@code ssh} and {@code scp}.
     */
    private       SshOptions   sshOptions;

    public Builder(String host) {
      this.program("ssh");
      this.options = new LinkedList<>();
      this.host = requireNonNull(host);
    }

    public Builder program(String program) {
      this.program = requireNonNull(program);
      return this;
    }

    public Builder user(String user) {
      this.user = requireNonNull(user);
      return this;
    }

    /**
     * Sets standard ssh options to this object.
     *
     * @param sshOptions Options to be set.
     * @return This object.
     */
    public Builder sshOptions(SshOptions sshOptions) {
      this.sshOptions = sshOptions;
      return this;
    }

    public Builder enableAuthAgentConnectionForwarding() {
      this.options.add("-A");
      return this;
    }

    public Shell build() {
      List<String> options = new LinkedList<String>() {{
        this.addAll(Builder.this.options);
        this.addAll(Builder.this.sshOptions.options(SshOptions.Formatter.forSsh()));
        this.add(
            user != null ?
                String.format("%s@%s", user, host) :
                host);
      }};
      return new Impl(program, options);
    }
  }
}
