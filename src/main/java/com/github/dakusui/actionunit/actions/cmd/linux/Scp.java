package com.github.dakusui.actionunit.actions.cmd.linux;

import com.github.dakusui.actionunit.actions.cmd.CommandLineComposer;
import com.github.dakusui.actionunit.actions.cmd.Commander;
import com.github.dakusui.actionunit.core.context.ContextFunction;
import com.github.dakusui.printables.PrintableFunction;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;

import static com.github.dakusui.actionunit.core.context.ContextFunctions.immediateOf;
import static com.github.dakusui.actionunit.utils.Checks.requireState;
import static java.util.Objects.requireNonNull;

public class Scp extends Commander<Scp> {
  private ContextFunction<Target> destination;

  public Scp(Function<String[], IntFunction<String>> parameterPlaceHolderFormatter) {
    super(parameterPlaceHolderFormatter);
    this.command("scp");
  }

  public Scp options(SshOptions options) {
    requireNonNull(options).options().forEach(this::addOption);
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
  protected CommandLineComposer buildCommandLineComposer() {
    Function<Target, String> formatTarget = PrintableFunction.of(Target::format).describe("Target::format");
    return commandLineComposerBuilderIfSet().clone()
        .append(" ", false)
        .append(requireState(Objects::nonNull, this.destination).andThen(formatTarget), true)
        .build();
  }

  public interface Target {
    Optional<SshOptions.Account> account();

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
        public Optional<SshOptions.Account> account() {
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
        public Optional<SshOptions.Account> account() {
          return Optional.of(SshOptions.Account.create(host));
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
        public Optional<SshOptions.Account> account() {
          return Optional.of(SshOptions.Account.create(user, host));
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
