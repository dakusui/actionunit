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
    return () -> UnixCommanderFactory.this;
  }

  default Cmd cmd() {
    return new Cmd(initializer());
  }
}
