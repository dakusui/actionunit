package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.actionunit.actions.cmd.unix.*;

public interface UnixCommanderFactory extends CommanderFactory {
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
    return new Scp(config(), config().sshOptions());
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

  class Impl extends CommanderFactory.Base implements UnixCommanderFactory {
    protected Impl(CommanderConfig commanderConfig, SshOptions sshOptions) {
      super(commanderConfig, sshOptions);
    }
  }

  class Builder extends CommanderFactory.Builder<Builder, UnixCommanderFactory> {

    @Override
    protected UnixCommanderFactory createCommanderFactory(CommanderConfig config, SshOptions sshOptions) {
      return new Impl(config, sshOptions);
    }
  }
}
