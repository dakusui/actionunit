package com.github.dakusui.actionunit.actions.cmd;

import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public interface CommanderFactoryManager<M extends CommanderFactoryManager<M, C>, C extends CommanderFactory> {
  C local();

  C remote(String host);

  class Base<M extends CommanderFactoryManager<M, C>, C extends CommanderFactory>
      implements CommanderFactoryManager<M, C> {
    final private Function<M, C>           localCommanderFactory;
    final private BiFunction<M, String, C> remoteCommanderFactory;

    public Base(Function<M, C> localCommanderFactory, BiFunction<M, String, C> remoteCommanderFactory) {
      this.localCommanderFactory = localCommanderFactory;
      this.remoteCommanderFactory = remoteCommanderFactory;
    }

    @SuppressWarnings("unchecked")
    @Override
    public C local() {
      return this.localCommanderFactory.apply((M) this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public C remote(String host) {
      return this.remoteCommanderFactory.apply((M) this, host);
    }
  }

  abstract class Builder<
      B extends Builder<B, M, C>,
      M extends CommanderFactoryManager<M, C>,
      C extends CommanderFactory> {

    private Function<M, C>           localCommanderFactory;
    private BiFunction<M, String, C> remoteCommanderFactory;

    @SuppressWarnings("unchecked")
    public B localCommanderFactory(Function<M, C> func) {
      this.localCommanderFactory = requireNonNull(func);
      return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B remoteCommanderFactory(BiFunction<M, String, C> func) {
      this.remoteCommanderFactory = requireNonNull(func);
      return (B) this;
    }

    public M build() {
      return createCommanderFactoryManager(localCommanderFactory, remoteCommanderFactory);
    }

    abstract protected M createCommanderFactoryManager(
        Function<M, C> localCommanderFactory,
        BiFunction<M, String, C> remoteCommanderFactory);
  }
}
