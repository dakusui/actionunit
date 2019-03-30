package com.github.dakusui.actionunit.ut.actions.cmd;

import com.github.dakusui.actionunit.actions.cmd.CommanderInitializer;
import com.github.dakusui.actionunit.actions.cmd.UnixCommanderFactory;
import com.github.dakusui.actionunit.actions.cmd.unix.Echo;
import com.github.dakusui.actionunit.actions.cmd.unix.SshShellBuilder;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import com.github.dakusui.processstreamer.core.process.Shell;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static com.github.dakusui.crest.Crest.asString;
import static com.github.dakusui.crest.Crest.assertThat;
import static com.github.dakusui.crest.Crest.substringAfterRegex;

@RunWith(Enclosed.class)
public class UnixCommanderFactoryTest {
  static abstract class Base implements UnixCommanderFactory {
    @Ignore
    @Test
    public void performLocally() {
      perform(localEcho().toAction());
    }

    @Ignore
    @Test
    public void performRemotely() throws UnknownHostException {
      perform(remoteEcho().toAction());
    }

    @Test
    public void printLocally() {
      assertThat(
          localEcho().buildCommandLineComposer().format(),
          asString(substringAfterRegex("echo").after("quoteWith").after("\\(hello world\\)").$()).isEmpty().$()
      );
    }

    @Test
    public void printRemotely() throws UnknownHostException {
      assertThat(
          remoteEcho().buildCommandLineComposer().format(),
          asString(substringAfterRegex("echo").after("quoteWith").after("\\(hello world\\)").$()).isEmpty().$()
      );
    }

    private Echo remoteEcho() throws UnknownHostException {
      return remote(InetAddress.getLocalHost().getHostName())
          .echo()
          .message("hello world")
          .downstreamConsumer(System.out::println);
    }

    private Echo localEcho() {
      return local().echo().message("hello world").downstreamConsumer(System.out::println);
    }

    private void perform(Action action) {
      ReportingActionPerformer.create().perform(action);
    }
  }

  public static class WithoutUsername extends Base {
    @Override
    public CommanderInitializer initializerFor(String host) {
      return new CommanderInitializer() {
        @Override
        public Shell shell() {
          return new SshShellBuilder(host)
              .program("ssh")
              .enableAuthAgentConnectionForwarding()
              .sshOptions(sshOptions())
              .build();
        }
      };
    }
  }

  public static class WithUsername extends Base {
    @Override
    public CommanderInitializer initializerFor(String host) {
      return new CommanderInitializer() {
        @Override
        public Shell shell() {
          return new SshShellBuilder(host)
              .program("ssh")
              .user(System.getProperty("user.name"))
              .enableAuthAgentConnectionForwarding()
              .sshOptions(sshOptions())
              .build();
        }
      };
    }
  }
}