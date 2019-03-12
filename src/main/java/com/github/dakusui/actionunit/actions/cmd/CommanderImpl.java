package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.processstreamer.core.process.Shell;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;

import static java.util.Objects.requireNonNull;

public class CommanderImpl extends CommanderBase<CommanderImpl> {
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

  private final CommandLineComposerFactory commandLineComposerFactory;
  private CommandLineComposer commandLineComposer;
  private String[] variableNames;

  public CommanderImpl(Shell shell, CommandLineComposerFactory commandLineComposerFactory) {
    super(shell);
    this.commandLineComposerFactory = requireNonNull(commandLineComposerFactory);
  }

  public CommanderImpl command(String commandLineFormat, String... variableNames) {
    requireNonNull(commandLineFormat);
    return command(this.commandLineComposerFactory.apply(commandLineFormat, variableNames), variableNames);
  }

  @Override
  protected CommandLineComposer commandLineComposer() {
    return this.commandLineComposer;
  }

  @Override
  protected String[] variableNames() {
    return this.variableNames;
  }

  private CommanderImpl command(CommandLineComposer commandLineComposer, String... variableNames) {
    this.commandLineComposer = requireNonNull(commandLineComposer);
    this.variableNames = variableNames;
    return this;
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
