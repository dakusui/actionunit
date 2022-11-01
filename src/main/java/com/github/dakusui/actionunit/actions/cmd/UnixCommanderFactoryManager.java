package com.github.dakusui.actionunit.actions.cmd;

import java.util.function.BiFunction;
import java.util.function.Function;

import static com.github.dakusui.actionunit.utils.InternalUtils.memoize;
import static java.util.Objects.requireNonNull;

public interface UnixCommanderFactoryManager extends CommanderFactoryManager<UnixCommanderFactory> {
  CommanderConfig configFor(String host);

  class Impl extends CommanderFactoryManager.Base<UnixCommanderFactoryManager, UnixCommanderFactory> implements UnixCommanderFactoryManager {
    private final Function<String, CommanderConfig> commanderConfigResolver;

    public Impl(
        Function<UnixCommanderFactoryManager, UnixCommanderFactory> localCommanderFactory,
        BiFunction<UnixCommanderFactoryManager, String, UnixCommanderFactory> remoteCommanderFactory, Function<String, CommanderConfig> commanderConfigResolver) {
      super(localCommanderFactory, remoteCommanderFactory);
      this.commanderConfigResolver = requireNonNull(commanderConfigResolver);
    }

    @Override
    public CommanderConfig configFor(String host) {
      return commanderConfigResolver.apply(host);
    }
  }

  class Builder extends CommanderFactoryManager.Builder<Builder, UnixCommanderFactoryManager, UnixCommanderFactory> {
    private Function<String, CommanderConfig> commanderConfigResolver;

    public Builder() {
      this.addConfigFor(host -> new CommanderConfig.Builder().build());
    }

    public Builder addConfigFor(Function<String, CommanderConfig> commanderConfigResolver) {
      this.commanderConfigResolver = requireNonNull(commanderConfigResolver);
      return this;
    }

    @Override
    protected UnixCommanderFactoryManager createCommanderFactoryManager(
        Function<UnixCommanderFactoryManager, UnixCommanderFactory> localCommanderFactory,
        BiFunction<UnixCommanderFactoryManager, String, UnixCommanderFactory> remoteCommanderFactory) {
      return new Impl(
          localCommanderFactory,
          remoteCommanderFactory,
          commanderConfigResolver
      );
    }
  }
}
