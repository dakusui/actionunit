package com.github.dakusui.actionunit.actions.cmd.unix;


import com.github.dakusui.processstreamer.core.process.Shell;

import java.util.LinkedList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class SshShellBuilder {
  private String       program;
  private String       user;
  private String       host;
  /**
   * Holds options specific to {@code ssh} command. That is, options supported
   * by {@code ssh} but not by {@code scp} should be stored in this field.
   */
  private List<String> options;
  /**
   * Holds options common to both {@code ssh} and {@code scp}.
   */
  private SshOptions   sshOptions;

  public SshShellBuilder(String host) {
    this.program("ssh");
    this.options = new LinkedList<>();
    this.host = requireNonNull(host);
  }

  public SshShellBuilder program(String program) {
    this.program = requireNonNull(program);
    return this;
  }

  public SshShellBuilder user(String user) {
    this.user = requireNonNull(user);
    return this;
  }

  /**
   * Sets standard ssh options to this object.
   *
   * @param sshOptions Options to be set.
   * @return This object.
   */
  public SshShellBuilder sshOptions(SshOptions sshOptions) {
    this.sshOptions = sshOptions;
    return this;
  }

  public SshShellBuilder enableAuthAgentConnectionForwarding() {
    this.options.add("-A");
    return this;
  }

  public Shell build() {
    List<String> options = new LinkedList<String>() {{
      this.addAll(SshShellBuilder.this.options);
      this.addAll(SshShellBuilder.this.sshOptions.options(SshOptions.Formatter.forSsh()));
      this.add(
          user != null ?
              String.format("%s@%s", user, host) :
              host);
    }};
    return new Shell() {
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
    };
  }
}
