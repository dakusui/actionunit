package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.actionunit.actions.cmd.unix.SshOptions;

import static java.util.Objects.requireNonNull;

public interface CommanderFactory {
  CommanderConfig config();

  SshOptions sshOptions();


  abstract class Base implements CommanderFactory {
    final CommanderConfig commanderConfig;
    final SshOptions      sshOptions;


    protected Base(CommanderConfig commanderConfig, SshOptions sshOptions) {
      this.commanderConfig = requireNonNull(commanderConfig);
      this.sshOptions = requireNonNull(sshOptions);
    }

    @Override
    public CommanderConfig config() {
      return this.commanderConfig;
    }

    @Override
    public SshOptions sshOptions() {
      return this.sshOptions;
    }
  }

  abstract class Builder<B extends Builder<B, C>, C extends CommanderFactory> {
    CommanderConfig config;
    SshOptions      sshOptions;

    protected Builder() {
      this.sshOptions(new SshOptions.Builder().build())
          .config(new CommanderConfig.Builder().build());
    }

    @SuppressWarnings("unchecked")
    public B config(CommanderConfig config) {
      this.config = requireNonNull(config);
      return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B sshOptions(SshOptions sshOptions) {
      this.sshOptions = requireNonNull(sshOptions);
      return (B) this;
    }

    public C build() {
      return createCommanderFactory(config, sshOptions);
    }

    protected abstract C createCommanderFactory(CommanderConfig config, SshOptions sshOptions);
  }
}
