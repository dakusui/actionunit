package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.actionunit.actions.RetryOption;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.context.ContextConsumer;
import com.github.dakusui.actionunit.core.context.ContextPredicate;
import com.github.dakusui.actionunit.core.context.StreamGenerator;
import com.github.dakusui.processstreamer.core.process.ProcessStreamer.Checker;
import com.github.dakusui.processstreamer.core.process.Shell;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public interface BackCommodore {

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

  abstract class Base implements BackCommodore {
    private Shell               shell;
    private RetryOption         retryOption;
    private Checker             checker;
    private Stream<String>      stdin;
    private Consumer<String>    downstreamConsumer;
    private Map<String, String> envvars;
    private File                cwd;

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
