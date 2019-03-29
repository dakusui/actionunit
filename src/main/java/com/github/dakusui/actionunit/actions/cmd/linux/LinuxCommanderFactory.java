package com.github.dakusui.actionunit.actions.cmd.linux;

import com.github.dakusui.actionunit.actions.cmd.Cmd;
import com.github.dakusui.actionunit.actions.cmd.CommanderFactory;

public interface LinuxCommanderFactory extends CommanderFactory {
  default Echo echo() {
    return createCommander(Echo::new);
  }

  default Cat cat() {
    return createCommander(Cat::new);
  }

  default Ls ls() {
    return createCommander(Ls::new);
  }

  default Mkdir mkdir() {
    return createCommander(Mkdir::new);
  }

  default Rm rm() {
    return createCommander(Rm::new);
  }

  default Touch touch() {
    return createCommander(Touch::new);
  }

  default Scp scp() {
    return createCommander(Scp::new);
  }

  default Curl curl() {
    return createCommander(Curl::new);
  }

  default Git git() {
    return () -> LinuxCommanderFactory.this;
  }

  default Cmd cmd() {
    return createCommander(Cmd::new);
  }
}
