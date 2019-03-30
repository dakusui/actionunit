package com.github.dakusui.actionunit.actions.cmd;

import com.github.dakusui.actionunit.actions.cmd.linux.SshShellBuilder;
import com.github.dakusui.processstreamer.core.process.Shell;

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
        host -> "localhost".equals(host) ?
            CommanderInitializer.INSTANCE :
            new CommanderInitializer() {
              @Override
              public Shell shell() {
                return new SshShellBuilder(host)
                    .program("ssh")
                    .enableAuthAgentConnectionForwarding()
                    .sshOptions(sshOptions())
                    .build();
              }
            });
  }
}
