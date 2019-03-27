package com.github.dakusui.actionunit.actions.cmd.linux;

import com.github.dakusui.processstreamer.core.process.Shell;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

import static java.util.Objects.requireNonNull;

/**
 * Models options common to both {@code ssh} and {@code scp} commands. (see below).
 * <pre>
 * -4               : IPv4
 * -6               : IPv6
 * -C               : compression
 * -c               : cipher spec
 * -F               : config file
 * -i               : identity
 * -o               : ssh option
 * -p (-P for scp)  : port
 * -q               : quiet
 * -v               : verbose
 * </pre>
 */
public interface SshOptions {
  boolean ipv4();

  boolean ipv6();

  boolean compression();

  Optional<String> cipherSpec();

  Optional<String> configFile();

  Optional<String> identity();

  List<String> sshOptions();

  OptionalInt port();

  boolean quiet();

  boolean verbose();

  default List<String> options(Formatter formatter) {
    return requireNonNull(formatter).format(this);
  };

  default Shell createSshShell(String program, String user, String host) {
    requireNonNull(program);
    requireNonNull(user);
    requireNonNull(host);
    return new SshShellBuilder(host).user(user)
        .enableAuthAgentConnectionForwarding()
        .program(program)
        .sshOptions(this)
        .build();
  }

  class Builder {
    private       String       identity;
    private final List<String> sshOptions = new LinkedList<>();

    public Builder() {
    }

    public Builder identity(String identity) {
      this.identity = requireNonNull(identity);
      return this;
    }

    public Builder addSshOption(String option) {
      sshOptions.add(option);
      return this;
    }

    public Builder addSshOption(String option, String value) {
      return this.addSshOption(String.format("%s=%s", option, value));
    }

    public Builder disablePasswordAuthentication() {
      return this.addSshOption("PasswordAuthentication", "no");
    }

    public Builder disableStrictHostkeyChecking() {
      return this.addSshOption("StrictHostkeyChecking", "no");
    }

    public SshOptions build() {
      return new SshOptions() {

        @Override
        public boolean ipv4() {
          return false;
        }

        @Override
        public boolean ipv6() {
          return false;
        }

        @Override
        public boolean compression() {
          return false;
        }

        @Override
        public Optional<String> cipherSpec() {
          return Optional.empty();
        }

        @Override
        public Optional<String> configFile() {
          return Optional.empty();
        }

        @Override
        public Optional<String> identity() {
          return Optional.ofNullable(identity);
        }

        @Override
        public List<String> sshOptions() {
          return sshOptions;
        }

        @Override
        public OptionalInt port() {
          return OptionalInt.empty();
        }

        @Override
        public boolean quiet() {
          return false;
        }

        @Override
        public boolean verbose() {
          return false;
        }

        @Override
        public List<String> options(Formatter formatter) {
          return requireNonNull(formatter).format(this);
        }
      };
    }
  }

  @FunctionalInterface
  interface Formatter {
    List<String> format(SshOptions sshOptions);
    static Formatter forSsh() {
      return sshOptions -> new LinkedList<String>() {{
        if (sshOptions.ipv4())
          add("-4");
        if (sshOptions.ipv6())
          add("-6");
        if (sshOptions.compression())
          add("-C");
        sshOptions.configFile().ifPresent(v -> {
          add("-c");
          add(v);
        });
        sshOptions.cipherSpec().ifPresent(v -> {
          add("-i");
          add(v);
        });
        sshOptions.identity().ifPresent(v -> {
          add("-i");
          add(v);
        });
        sshOptions.sshOptions().forEach(v -> {
          add("-o");
          add(v);
        });
        sshOptions.port().ifPresent(v -> {
          add("-p");
          add(Objects.toString(v));
        });
        if (sshOptions.quiet())
          add("-q");
        if (sshOptions.verbose())
          add("-v");
      }};
    }
    static Formatter forScp() {
      return sshOptions -> new LinkedList<String>() {{
        if (sshOptions.ipv4())
          add("-4");
        if (sshOptions.ipv6())
          add("-6");
        if (sshOptions.compression())
          add("-C");
        sshOptions.configFile().ifPresent(v -> {
          add("-c");
          add(v);
        });
        sshOptions.cipherSpec().ifPresent(v -> {
          add("-i");
          add(v);
        });
        sshOptions.identity().ifPresent(v -> {
          add("-i");
          add(v);
        });
        sshOptions.sshOptions().forEach(v -> {
          add("-o");
          add(v);
        });
        sshOptions.port().ifPresent(v -> {
          add("-P");
          add(Objects.toString(v));
        });
        if (sshOptions.quiet())
          add("-q");
        if (sshOptions.verbose())
          add("-v");
      }};
    }
  }
}
