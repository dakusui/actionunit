package com.github.dakusui.actionunit.actions.cmd.unix;

import com.github.dakusui.actionunit.actions.cmd.Commander;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
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

  List<String> jumpHosts();

  Optional<String> cipherSpec();

  Optional<String> configFile();

  Optional<String> identity();

  List<String> sshOptions();

  OptionalInt port();

  boolean quiet();

  boolean verbose();

  default List<String> options(Formatter formatter) {
    return requireNonNull(formatter).format(this);
  }

  class Builder {
    private final SshOptions   base;
    private       String       identity;
    private final List<String> sshOptions = new LinkedList<>();
    private final List<String> jumpHosts  = new LinkedList<>();

    public Builder() {
      this(new SshOptions() {

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
        public List<String> jumpHosts() {
          return emptyList();
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
          return Optional.empty();
        }

        @Override
        public List<String> sshOptions() {
          return emptyList();
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
      });
    }

    public Builder(SshOptions base) {
      this.base = requireNonNull(base);
    }


    public Builder identity(String identity) {
      this.identity = requireNonNull(identity);
      return this;
    }

    public Builder addJumpHost(String jumpHost) {
      this.jumpHosts.add(requireNonNull(jumpHost));
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
          return base.ipv4();
        }

        @Override
        public boolean ipv6() {
          return base.ipv6();
        }

        @Override
        public boolean compression() {
          return base.compression();
        }

        @Override
        public List<String> jumpHosts() {
          return jumpHosts;
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
          add("-F");
          add(v);
        });
        if (!sshOptions.jumpHosts().isEmpty())
          add("-J " + String.join(",", sshOptions.jumpHosts()));
        sshOptions.cipherSpec().ifPresent(v -> {
          add("-c");
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
          add("-F");
          add(v);
        });
        if (!sshOptions.jumpHosts().isEmpty())
          add("-J " + String.join(",", sshOptions.jumpHosts()));
        sshOptions.cipherSpec().ifPresent(v -> {
          add("-c");
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
