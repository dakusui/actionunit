package com.github.dakusui.actionunit.actions.cmd;

public interface UnixCommanderFactory extends CommanderFactory {
  HostCommanderFactory local();

  HostCommanderFactory remote(String host);
}
