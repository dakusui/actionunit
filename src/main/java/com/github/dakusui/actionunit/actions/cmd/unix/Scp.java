package com.github.dakusui.actionunit.actions.cmd.unix;

import com.github.dakusui.actionunit.actions.cmd.CommandLineComposer;
import com.github.dakusui.actionunit.actions.cmd.Commander;
import com.github.dakusui.actionunit.actions.cmd.CommanderConfig;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.context.ContextFunction;
import com.github.dakusui.printables.PrintableFunction;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static com.github.dakusui.actionunit.core.context.ContextFunctions.immediateOf;
import static com.github.dakusui.actionunit.utils.Checks.requireState;
import static java.util.Objects.requireNonNull;

public class Scp extends Commander<Scp> {
  private ContextFunction<Target>         destination;
  private List<Function<Context, Target>> files;
  private Function<String, SshOptions>    sshOptionsResolver;

  public Scp(CommanderConfig config) {
    super(config, "scp");
    this.files = new LinkedList<>();
    sshOptionsResolver(config.sshOptionsResolver());
  }

  public Scp sshOptionsResolver(Function<String, SshOptions> resolver) {
    this.sshOptionsResolver = requireNonNull(resolver);
    return this;
  }

  public Scp recursive() {
    return this.addOption("-r");
  }

  public Scp file(Function<Context, Target> target) {
    this.files.add(requireNonNull(target));
    return this;
  }

  public Scp file(Target target) {
    return this.file(immediateOf(requireNonNull(target)));
  }

  public Scp to(Target target) {
    this.destination = immediateOf(requireNonNull(target));
    return this;
  }

  @Override
  public CommandLineComposer buildCommandLineComposer() {
    Scp cloned = this.clone();
    CommandLineComposer.Builder commandLineComposerBuilder = cloned.commandLineComposerBuilderIfSet();
    Function<Target, String> formatTarget = PrintableFunction.of(Target::format).describe("Target::format");
    for (Function<Context, Target> each : files) {
      commandLineComposerBuilder
          .append(" ", false)
          .append(each.andThen(formatTarget), true);
    }
    return commandLineComposerBuilder
        .append(" ", false)
        .append(requireState(Objects::nonNull, cloned.destination).andThen(formatTarget), true)
        .build();
  }

  @Override
  public Scp clone() {
    Scp ret = super.clone();
    ret.sshOptionsResolver.apply("{remotehost}").formatOptionsWith(SshOptions.Formatter.forScp()).forEach(ret::addOption);
    ret.files = new ArrayList<>(ret.files);
    return ret;
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

    static Target of(String path) {
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

    static Target of(String host, String path) {
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

    static Target of(String user, String host, String path) {
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
