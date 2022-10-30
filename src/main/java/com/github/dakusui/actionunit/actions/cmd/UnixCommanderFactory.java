package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.actionunit.actions.cmd.unix.*;

import java.util.function.Function;

public interface UnixCommanderFactory extends CommanderFactory {

  static UnixCommanderFactory createForLocal(ShellManager manager) {
    return create(manager, "localhost");
  }

  static UnixCommanderFactory create(ShellManager manager, String host) {
    return new Builder()
        .config(CommanderConfig.builder()
            .shell(manager.shellFor(host))
            .build())
        .build();
  }

  default Echo echo() {
    return new Echo(config());
  }

  default Cat cat() {
    return new Cat(config());
  }

  default Ls ls() {
    return new Ls(config());
  }

  default Mkdir mkdir() {
    return new Mkdir(config());
  }

  default Rm rm() {
    return new Rm(config());
  }

  default Touch touch() {
    return new Touch(config());
  }

  default Scp scp() {
    return new Scp(config(), this::sshOptionsFor);
  }

  default Curl curl() {
    return new Curl(config());
  }

  default Git git() {
    return new Git.Builder().build();
  }

  default Cmd cmd() {
    return new Cmd(config());
  }


  SshOptions sshOptionsFor(String host);

  class Impl extends CommanderFactory.Base implements UnixCommanderFactory {
    protected Impl(CommanderConfig commanderConfig, Function<String, SshOptions> sshOptionsResolver) {
      super(commanderConfig, sshOptionsResolver);
    }
  }

  class Builder extends CommanderFactory.Builder<Builder, UnixCommanderFactory> {

    @Override
    protected UnixCommanderFactory createCommanderFactory(CommanderConfig config, Function<String, SshOptions> sshOptionsResolver) {
      return new Impl(config, sshOptionsResolver);
    }
  }
}
