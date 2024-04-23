package com.github.dakusui.actionunit.ut.actions.cmd;

import com.github.dakusui.actionunit.actions.cmd.ShellManager;
import com.github.dakusui.actionunit.actions.cmd.UnixCommanderFactory;
import com.github.dakusui.actionunit.actions.cmd.unix.SshOptions;
import com.github.dakusui.actionunit.core.Context;
import org.junit.Test;

public class UnixCommanderFactoryExample {
  @Test
  public void example1() {
    ShellManager shellManager = new ShellManager.Default.Builder()
        .sshOptionsResolver(h -> new SshOptions.Builder()
            .authAgentConnectionForwarding(true)
            .disableStrictHostkeyChecking()
            .disablePasswordAuthentication()
            .build())
        .build();

    UnixCommanderFactory commanderFactory = UnixCommanderFactory.create(shellManager);

    commanderFactory.cat().beginHereDocument("HELLO")
        .writeln("hello")
        .writeln("world")
        .endHereDocument()
        .toContextConsumer()
        .accept(Context.create());
  }
}
