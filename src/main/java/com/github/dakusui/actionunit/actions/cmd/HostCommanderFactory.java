package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.actionunit.actions.cmd.linux.Cat;
import com.github.dakusui.actionunit.actions.cmd.linux.Cmd;
import com.github.dakusui.actionunit.actions.cmd.linux.Curl;
import com.github.dakusui.actionunit.actions.cmd.linux.Echo;
import com.github.dakusui.actionunit.actions.cmd.linux.Git;
import com.github.dakusui.actionunit.actions.cmd.linux.Ls;
import com.github.dakusui.actionunit.actions.cmd.linux.Mkdir;
import com.github.dakusui.actionunit.actions.cmd.linux.Rm;
import com.github.dakusui.actionunit.actions.cmd.linux.Scp;
import com.github.dakusui.actionunit.actions.cmd.linux.Touch;

public interface HostCommanderFactory extends CommanderFactory {
  default Echo echo() {
    return new Echo(initializer());
  }

  default Cat cat() {
    return new Cat(initializer());
  }

  default Ls ls() {
    return new Ls(initializer());
  }

  default Mkdir mkdir() {
    return new Mkdir(initializer());
  }

  default Rm rm() {
    return new Rm(initializer());
  }

  default Touch touch() {
    return new Touch(initializer());
  }

  default Scp scp() {
    return  new Scp(initializer());
  }

  default Curl curl() {
    return new Curl(initializer());
  }

  default Git git() {
    return () -> HostCommanderFactory.this;
  }

  default Cmd cmd() {
    return new Cmd(initializer());
  }

  @Override
  default CommanderInitializer initializer() {
    return CommanderInitializer.INSTANCE;
  }
}
