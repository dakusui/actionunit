package com.github.dakusui.actionunit.actions.cmd;

import java.util.function.Function;

import static com.github.dakusui.actionunit.utils.InternalUtils.memoize;

public interface CommanderFactoryManager {
  default UnixCommanderFactory local() {
    return () -> configManager().apply("localhost");
  }

  default UnixCommanderFactory remote(String host) {
    return () -> configManager().apply(host);
  }

  default Function<String, CommanderConfig> configManager() {
    return memoize(this::configFor);
  }

  CommanderConfig configFor(String host);
}
