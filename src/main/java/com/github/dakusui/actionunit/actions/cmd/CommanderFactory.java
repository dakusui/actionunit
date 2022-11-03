package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.actionunit.actions.cmd.unix.SshOptions;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public interface CommanderFactory {
  CommanderConfig config();

  abstract class Base implements CommanderFactory {
    final CommanderConfig              commanderConfig;


    protected Base(CommanderConfig commanderConfig) {
      this.commanderConfig = requireNonNull(commanderConfig);
    }

    @Override
    public CommanderConfig config() {
      return this.commanderConfig;
    }
  }

  abstract class Builder<B extends Builder<B, C>, C extends CommanderFactory> {
    CommanderConfig              config;
    Function<String, SshOptions> sshOptionsResolver;

    protected Builder() {
      this.sshOptionsResolver(h -> new SshOptions.Builder().build())
          .config(new CommanderConfig.Builder().build());
    }

    @SuppressWarnings("unchecked")
    public B config(CommanderConfig config) {
      this.config = requireNonNull(config);
      return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B sshOptionsResolver(Function<String, SshOptions> sshOptionsResolver) {
      this.sshOptionsResolver = requireNonNull(sshOptionsResolver);
      return (B) this;
    }

    public C build() {
      return createCommanderFactory(config, sshOptionsResolver);
    }

    protected abstract C createCommanderFactory(CommanderConfig config, Function<String, SshOptions> sshOptions);
  }
}
