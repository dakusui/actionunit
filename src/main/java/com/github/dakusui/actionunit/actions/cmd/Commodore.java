package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.context.ContextConsumer;
import com.github.dakusui.actionunit.core.context.ContextPredicate;
import com.github.dakusui.actionunit.core.context.StreamGenerator;
import com.github.dakusui.processstreamer.core.process.ProcessStreamer.Checker;
import com.github.dakusui.processstreamer.core.process.Shell;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import static java.util.Objects.requireNonNull;

public interface Commodore {
  enum CommandLineComposerFactory implements BiFunction<String, String[], CommandLineComposer> {
    BY_INDEX {
      public CommandLineComposer apply(String commandLineFormat, String[] variableNames) {
        return CommandLineComposer.byVariableName(commandLineFormat);
      }
    },
    BY_KNOWN_VARIABLE_NAME {
      public CommandLineComposer apply(String commandLineFormat, String[] variableNames) {
        return CommandLineComposer.byIndex(commandLineFormat);
      }
    };
  }

  class RetryOption {
    public static RetryOption timeoutInSeconds(long timeoutInSeconds) {
      return new RetryOption(
          timeoutInSeconds,
          TimeUnit.SECONDS,
          Throwable.class,
          0,
          TimeUnit.SECONDS,
          0);
    }

    final long                       timeoutDuration;
    final TimeUnit                   timeoutTimeUnit;
    final Class<? extends Throwable> retryOn;
    final long                       retryInterval;
    final TimeUnit                   retryIntervalTimeUnit;
    final int                        retries;


    RetryOption(
        long timeoutDuration,
        TimeUnit timeoutTimeUnit,
        Class<? extends Throwable> retryOn,
        long retryInterval,
        TimeUnit retryIntervalTimeUnit,
        int retries) {
      this.timeoutDuration = timeoutDuration;
      this.timeoutTimeUnit = requireNonNull(timeoutTimeUnit);
      this.retryOn = requireNonNull(retryOn);
      this.retryInterval = retryInterval;
      this.retryIntervalTimeUnit = requireNonNull(retryIntervalTimeUnit);
      this.retries = retries;
    }
  }

  default CommandLineComposerFactory commandLineComposerFactory() {
    return CommandLineComposerFactory.BY_INDEX;
  }

  default Optional<Shell> shell() {
    return Optional.empty();
  }

  RetryOption retryOption();

  Checker checker();

  ContextConsumer toContextConsumer(String commandLineFormat, String... varnames);

  ContextPredicate toContextPredicate(String commandLineFormat, String... varnames);

  StreamGenerator<String> toStreamGenerator(String commandLineFormat, String... varnames);

  Action toAction(Checker checker, RetryOption retryOption);

  ContextConsumer toContextConsumerWith(Checker checker, RetryOption retryOption);

  abstract class Base implements Commodore {
    private final Shell       shell;
    private final RetryOption retryOption;
    private final Checker     checker;

    @Override
    public Optional<Shell> shell() {
      return Optional.ofNullable(shell);
    }

    @Override
    public RetryOption retryOption() {
      return this.retryOption;
    }

    @Override
    public Checker checker() {
      return this.checker;
    }

    public Base(Shell shell, RetryOption retryOption, Checker checker) {
      this.shell = shell;
      this.retryOption = retryOption;
      this.checker = checker;
    }
  }

  class Simple extends Base {
    public Simple(Shell shell, RetryOption retryOption, Checker checker) {
      super(shell, retryOption, checker);
    }

    @Override
    public ContextConsumer toContextConsumer(String commandLineFormat, String... varnames) {
      return null;
    }


    @Override
    public ContextPredicate toContextPredicate(String commandLineFormat, String... varnames) {
      return null;
    }

    @Override
    public StreamGenerator<String> toStreamGenerator(String commandLineFormat, String... varnames) {
      return null;
    }

    @Override
    public Action toAction(Checker checker, RetryOption retryOption) {
      return null;
    }

    @Override
    public ContextConsumer toContextConsumerWith(Checker checker, RetryOption retryOption) {
      return null;
    }
  }

  class Builder {
    private Shell       shell;
    private RetryOption retryOption;
    private Checker     checker;

    public Builder() {
      this.checker = Checker.createCheckerForExitCode(0);
      this.retryOption = RetryOption.timeoutInSeconds(60);
      this.shell = null;
    }

    public Commodore build() {
      return new Simple(shell, retryOption, checker);
    }
  }

  interface Factory {
    default CommanderImpl commander(String host) {
      return new CommanderImpl(shellFor(host), commandLineComposerFactory());
    }

    Shell shellFor(String host);

    CommandLineComposerFactory commandLineComposerFactory();

    class Builder {
      Map<String, Shell> shells = new HashMap<>();
      private CommandLineComposerFactory commandLineComposerFactory;

      public Builder() {
        this.commandLineComposerFactory(CommandLineComposerFactory.BY_INDEX)
            .addLocal("localhost")
            .addLocal("localhost.localdomain");
      }

      public Builder addRemote(String user, String host, String identity) {
        this.shells.put(
            host,
            new Shell.Builder.ForSsh(host)
                .addOption("-A")
                .userName(user)
                .identity(identity)
                .build());
        return this;
      }

      public Builder addLocal(String host) {
        this.shells.put(host, Shell.local());
        return this;
      }

      public Builder commandLineComposerFactory(CommandLineComposerFactory commandLineComposerFactory) {
        this.commandLineComposerFactory = requireNonNull(commandLineComposerFactory);
        return this;
      }

      public Factory build() {
        return new Factory() {
          @Override
          public Shell shellFor(String host) {
            if (shells.containsKey(requireNonNull(host)))
              return shells.get(host);
            throw new NoSuchElementException("Unknown host:" + host);
          }

          @Override
          public CommandLineComposerFactory commandLineComposerFactory() {
            return commandLineComposerFactory;
          }
        };
      }
    }
  }
}
