package com.github.dakusui.actionunit.actions.cmd.linux;

import com.github.dakusui.processstreamer.core.process.Shell;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public interface SshOptions {
  Optional<String> identity();

  Account account();

  List<String> options();

  default Shell createShell(String program) {
    requireNonNull(program);
    return new Shell() {
      @Override
      public String program() {
        return program;
      }

      @Override
      public List<String> options() {
        Stream<String> identity = Stream.of("-i", identity().get());
        Stream<String> opts = SshOptions.this.options().stream();
        return Stream.concat(
            identity().isPresent() ?
                Stream.concat(identity, opts) :
                opts,
            Stream.of(account().format())).collect(toList());
      }

      @Override
      public String toString() {
        return Stream.concat(
            Stream.of(program()),
            options().stream()
        ).collect(Collectors.joining(" "));
      }
    };
  }

  class Builder {
    private final String       hostname;
    private       String       user;
    private       String       identity;
    private final List<String> options = new LinkedList<>();

    public Builder(String hostname) {
      this.hostname = requireNonNull(hostname);
    }

    public Builder user(String user) {
      this.user = requireNonNull(user);
      return this;
    }

    public Builder identity(String identity) {
      this.identity = requireNonNull(identity);
      return this;
    }

    public Builder addOption(String option) {
      options.add(option);
      return this;
    }

    public Builder addSshConfigOption(String option) {
      return this.addOption("-o").addOption(option);
    }

    public Builder addSshConfigOption(String option, String value) {
      return this.addSshConfigOption(String.format("%s=%s", option, value));
    }

    public Builder disablePasswordAuthentication() {
      return this.addSshConfigOption("PasswordAuthentication", "no");
    }

    public Builder disableStrictHostkeyChecking() {
      return this.addSshConfigOption("StrictHostkeyChecking", "no");
    }

    public SshOptions build() {
      return new SshOptions() {
        @Override
        public Optional<String> identity() {
          return Optional.ofNullable(identity);
        }

        @Override
        public Account account() {
          return new Account() {
            @Override
            public Optional<String> user() {
              return Optional.ofNullable(user);
            }

            @Override
            public String hostname() {
              return hostname;
            }
          };
        }

        @Override
        public List<String> options() {
          return unmodifiableList(new ArrayList<>(options));
        }
      };
    }
  }

  interface Account {
    Optional<String> user();

    String hostname();

    default String format() {
      return user().isPresent() ?
          String.format("%s@%s", user().get(), hostname()) :
          hostname();
    }

    static Account create(String hostname) {
      requireNonNull(hostname);
      return new Account() {
        @Override
        public Optional<String> user() {
          return Optional.empty();
        }

        @Override
        public String hostname() {
          return hostname;
        }
      };
    }

    static Account create(String user, String hostname) {
      requireNonNull(user);
      requireNonNull(hostname);
      return new Account() {
        @Override
        public Optional<String> user() {
          return Optional.of(user);
        }

        @Override
        public String hostname() {
          return hostname;
        }
      };
    }
  }
}
