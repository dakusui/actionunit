package com.github.dakusui.actionunit.actions.cmd.linux;

import com.github.dakusui.actionunit.actions.cmd.CommandLineComposer;
import com.github.dakusui.actionunit.actions.cmd.Commander;
import com.github.dakusui.actionunit.actions.cmd.CommanderInitializer;
import com.github.dakusui.actionunit.core.context.ContextFunction;
import com.github.dakusui.printables.PrintableFunction;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static com.github.dakusui.actionunit.core.context.ContextFunctions.immediateOf;
import static com.github.dakusui.actionunit.utils.Checks.requireState;
import static java.util.Objects.requireNonNull;

public class Scp extends Commander<Scp> {
  private ContextFunction<Target> destination;
  private SshOptions              sshOptions;

  public Scp(CommanderInitializer initializer) {
    super(initializer);
    initializer.init(this);
  }

  public Scp options(SshOptions sshOptions) {
    this.sshOptions = requireNonNull(sshOptions);
    return this;
  }

  public Scp recursive() {
    return this.addOption("-r");
  }

  public Scp file(Target target) {
    return this.add(requireNonNull(target).format());
  }

  public Scp to(Target target) {
    this.destination = immediateOf(requireNonNull(target));
    return this;
  }

  @Override
  public CommandLineComposer buildCommandLineComposer() {
    Scp cloned = this.clone();
    if (sshOptions != null)
      sshOptions.options(SshOptions.Formatter.forScp()).forEach(cloned::addOption);
    CommandLineComposer.Builder commandLineComposerBuilder = cloned.commandLineComposerBuilderIfSet();
    Function<Target, String> formatTarget = PrintableFunction.of(Target::format).describe("Target::format");
    return commandLineComposerBuilder
        .append(" ", false)
        .append(requireState(Objects::nonNull, this.destination).andThen(formatTarget), true)
        .build();
  }

  public interface Target {
    interface Account {
      Optional<String> user();

      String host();

      default String format() {
        return user()
            .map(v -> String.format("%s@%s", v, host()))
            .orElseGet(this::host);
      }

      static Account create(String user, String host) {
        requireNonNull(user);
        requireNonNull(host);
        return new Account() {
          @Override
          public Optional<String> user() {
            return Optional.of(user);
          }

          @Override
          public String host() {
            return host;
          }

          @Override
          public String toString() {
            return format();
          }
        };
      }

      static Account create(String host) {
        requireNonNull(host);
        return new Account() {
          @Override
          public Optional<String> user() {
            return Optional.empty();
          }

          @Override
          public String host() {
            return host;
          }

          @Override
          public String toString() {
            return format();
          }
        };
      }
    }

    Optional<Account> account();

    String path();

    default String format() {
      return account().isPresent() ?
          String.format("%s:%s", account().get().format(), path()) :
          path();
    }

    static Target create(String path) {
      requireNonNull(path);
      return new Target() {
        @Override
        public Optional<Account> account() {
          return Optional.empty();
        }

        @Override
        public String path() {
          return path;
        }

        @Override
        public String toString() {
          return format();
        }
      };
    }

    static Target create(String host, String path) {
      requireNonNull(host);
      requireNonNull(path);
      return new Target() {
        @Override
        public Optional<Account> account() {
          return Optional.of(Account.create(host));
        }

        @Override
        public String path() {
          return path;
        }

        @Override
        public String toString() {
          return format();
        }
      };
    }

    static Target create(String user, String host, String path) {
      requireNonNull(user);
      requireNonNull(host);
      requireNonNull(path);
      return new Target() {
        @Override
        public Optional<Account> account() {
          return Optional.of(Account.create(user, host));
        }

        @Override
        public String path() {
          return path;
        }

        @Override
        public String toString() {
          return format();
        }
      };
    }
  }
}
