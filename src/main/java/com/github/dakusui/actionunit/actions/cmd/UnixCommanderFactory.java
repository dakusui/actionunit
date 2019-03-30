package com.github.dakusui.actionunit.actions.cmd;

import java.util.function.Function;

import static com.github.dakusui.actionunit.utils.InternalUtils.memoize;

public interface UnixCommanderFactory {
  default HostCommanderFactory local() {
    return () -> initializerManager().apply("localhost");
  }

  default HostCommanderFactory remote(String host) {
    return () -> initializerManager().apply(host);
  }

  default Function<String, CommanderInitializer> initializerManager() {
    return memoize(
        host -> isLocalHost(host) ?
            CommanderInitializer.DEFAULT_INSTANCE :
            initializerFor(host)
    );
  }

  default boolean isLocalHost(String host) {
    return "localhost".equals(host);
  }

  CommanderInitializer initializerFor(String host);
}
