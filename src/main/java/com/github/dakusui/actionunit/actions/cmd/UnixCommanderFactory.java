package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.actionunit.actions.cmd.unix.Cat;
import com.github.dakusui.actionunit.actions.cmd.unix.Cmd;
import com.github.dakusui.actionunit.actions.cmd.unix.Curl;
import com.github.dakusui.actionunit.actions.cmd.unix.Echo;
import com.github.dakusui.actionunit.actions.cmd.unix.Git;
import com.github.dakusui.actionunit.actions.cmd.unix.Ls;
import com.github.dakusui.actionunit.actions.cmd.unix.Mkdir;
import com.github.dakusui.actionunit.actions.cmd.unix.Rm;
import com.github.dakusui.actionunit.actions.cmd.unix.Scp;
import com.github.dakusui.actionunit.actions.cmd.unix.Touch;

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
    return new Scp(config());
  }

  default Curl curl() {
    return new Curl(config());
  }

  default Git git() {
    return () -> UnixCommanderFactory.this;
  }

  default Cmd cmd() {
    return new Cmd(config());
  }
}
