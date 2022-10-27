package com.github.dakusui.actionunit.ut.actions.cmd;

import com.github.dakusui.actionunit.actions.cmd.CommanderConfig;
import com.github.dakusui.actionunit.actions.cmd.CommanderFactoryManager;
import com.github.dakusui.actionunit.actions.cmd.unix.*;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.function.Function;

import static com.github.dakusui.crest.Crest.*;
import static com.github.dakusui.pcond.forms.Predicates.isEmptyString;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

@RunWith(Enclosed.class)
public class CommanderFactoryManagerTest {
  final static SshOptions DEFAULT_SSH_OPTIONS_IN_COMMAND_FACTORY_MANAGER_TEST = new SshOptions.Impl(true, false, true, emptyList(), null, "ssh_config", null, emptyList(), 9999, false, true);

  static abstract class Base implements CommanderFactoryManager {
    @Ignore
    @Test
    public void performLocally() {
      perform(localEcho().toAction());
    }

    @Ignore
    @Test
    public void performRemotely() {
      perform(remoteEcho().toAction());
    }

    @Test
    public void formatLocalEcho() {
      assertThat(
          localEcho(),
          allOf(
              asString(
                  call("buildCommandLineComposer")
                      .andThen("compose", Context.create()).$())
                  .check(
                      substringAfterRegex("echo")
                          .after("'hello world'").$(),
                      isEmptyString()
                  ).$(),
              asString(
                  call("shell")
                      .andThen("format").$())
                  .check(
                      substringAfterRegex("sh").after("-c").$(),
                      isEmptyString()).$()
          )
      );
    }

    @Test
    public void formatRemoteEcho() {
      assertThat(
          remoteEcho(),
          allOf(
              asString(
                  call("buildCommandLineComposer")
                      .andThen("compose", Context.create()).$())
                  .check(substringAfterRegex("echo").after("'hello world'").$(), isEmptyString()).$(),
              asString(call("shell").andThen("format").$())
                  .check(substringAfterExpectedRegexesForSshOptions(), isEmptyString()).$()
          )
      );
    }

    @Test
    public void formatScp() {
      assertThat(
          scp(),
          asString(
              call("buildCommandLineComposer")
                  .andThen("compose", Context.create()).$())
              .check(
                  substringAfterExpectedRegexesForSshOptions_Scp(),
                  isEmptyString()
              ).$());
    }


    abstract public Function<String, String> substringAfterExpectedRegexesForSshOptions();

    abstract Function<String, String> substringAfterExpectedRegexesForSshOptions_Scp();

    private Echo remoteEcho() {
      return remote(hostName())
          .echo()
          .message("hello world")
          .downstreamConsumer(System.out::println);
    }

    private Echo localEcho() {
      return local().echo().message("hello world").downstreamConsumer(System.out::println);
    }

    private Scp scp() {
      return local()
          .scp().file(Scp.Target.of("/local/file"))
          .to(Scp.Target.of("user", "host", "/remote/file"));
    }

    private void perform(Action action) {
      ReportingActionPerformer.create().perform(action);
    }
  }

  public static class WithoutUsername extends Base {
    @Override
    public CommanderConfig configFor(String host) {
      return "localhost".equals(host) ?
          CommanderConfig.DEFAULT :
          CommanderConfig.builder().shell(
                  new SshShell.Builder(host)
                      .program("ssh")
                      .sshOptions(new SshOptions.Builder(DEFAULT_SSH_OPTIONS_IN_COMMAND_FACTORY_MANAGER_TEST)
                          .disableStrictHostkeyChecking()
                          .disablePasswordAuthentication()
                          .build())
                      .enableAuthAgentConnectionForwarding()
                      .build())
              .build();
    }

    @Override
    public Function<String, String> substringAfterExpectedRegexesForSshOptions() {
      return substringAfterRegex("ssh")
          .after("-A")
          .after("-o StrictHostkeyChecking=no")
          .after("-o PasswordAuthentication=no")
          .after(hostName())
          .$();
    }

    @Override
    Function<String, String> substringAfterExpectedRegexesForSshOptions_Scp() {
      return substringAfterRegex("scp")
          .after("-o").after("StrictHostkeyChecking=no")
          .after("-o").after("PasswordAuthentication=no")
          .after("'/local/file'")
          .after("'user@host:/remote/file'")
          .$();
    }
  }

  public static class WithUsername extends Base {
    @Override
    public CommanderConfig configFor(String host) {
      return "localhost".equals(host) ?
          CommanderConfig.DEFAULT :
          CommanderConfig.builder()
              .shell(new SshShell.Builder(host)
                  .program("ssh")
                  .user(userName())
                  .sshOptions(createSshOptions())
                  .enableAuthAgentConnectionForwarding()
                  .build())
              .build();
    }

    private static SshOptions createSshOptions() {
      return new SshOptions.Builder(DEFAULT_SSH_OPTIONS_IN_COMMAND_FACTORY_MANAGER_TEST)
          .disableStrictHostkeyChecking()
          .disablePasswordAuthentication()
          .build();
    }


    @Override
    public Function<String, String> substringAfterExpectedRegexesForSshOptions() {
      return substringAfterRegex("ssh")
          .after("-A")
          .after("-o StrictHostkeyChecking=no")
          .after("-o PasswordAuthentication=no")
          .after(String.format("%s@%s", userName(), hostName()))
          .$();
    }

    @Override
    Function<String, String> substringAfterExpectedRegexesForSshOptions_Scp() {
      return substringAfterRegex("scp")
          .after("-o").after("StrictHostkeyChecking=no")
          .after("-o").after("PasswordAuthentication=no")
          .after("'/local/file'")
          .after("'user@host:/remote/file'")
          .$();
    }
  }

  public static class WithCustomSshOptions1 extends Base {
    @Override
    public CommanderConfig configFor(String host) {
      return "localhost".equals(host) ?
          CommanderConfig.builder().sshOptions(DEFAULT_SSH_OPTIONS_IN_COMMAND_FACTORY_MANAGER_TEST).build() :
          CommanderConfig.builder()
              .shell(new SshShell.Builder(host)
                  .program("ssh")
                  .user(userName())
                  .enableAuthAgentConnectionForwarding()
                  .sshOptions(DEFAULT_SSH_OPTIONS_IN_COMMAND_FACTORY_MANAGER_TEST)
                  .build())
              .build();
    }

    @Override
    public Function<String, String> substringAfterExpectedRegexesForSshOptions() {
      return substringAfterRegex("ssh")
          .after("-A")
          .after("-4")
          .after("-F")
          .after("-p 9999")
          .after("-v")
          .after(String.format("%s@%s", userName(), hostName()))
          .$();
    }

    @Override
    Function<String, String> substringAfterExpectedRegexesForSshOptions_Scp() {
      return substringAfterRegex("scp")
          .after("-4")
          .after("-C")
          .after("-F ssh_config")
          .after("-P 9999")
          .after("-v")
          .after("'/local/file'")
          .after("'user@host:/remote/file'")
          .$();
    }
  }

  public static class WithCustomSshOptions2 extends Base {
    private final SshOptions sshOptions = new SshOptions.Impl(false, true, false, asList("jumphost1", "jumphost2"), "cipher_spec", null, "id_rsa", emptyList(), null, true, false);

    @Override
    public CommanderConfig configFor(String host) {
      return "localhost".equals(host) ?
          CommanderConfig.builder().sshOptions(sshOptions).build() :
          CommanderConfig.builder().shell(new SshShell.Builder(host)
              .program("ssh")
              .sshOptions(sshOptions)
              .user(userName())
              .enableAuthAgentConnectionForwarding()
              .build()).build();
    }

    @Override
    public Function<String, String> substringAfterExpectedRegexesForSshOptions() {
      return substringAfterRegex("ssh")
          .after("-A")
          .after("-6")
          .after("-c cipher_spec")
          .after("-i id_rsa")
          .after("-q")
          .after(String.format("%s@%s", userName(), hostName()))
          .$();
    }

    @Override
    Function<String, String> substringAfterExpectedRegexesForSshOptions_Scp() {
      return substringAfterRegex("scp")
          .after("-6")
          .after("-c cipher_spec")
          .after("-i id_rsa")
          .after("-q")
          .after("'/local/file'")
          .after("'user@host:/remote/file'")
          .$();
    }

  }

  public static String hostName() {
    try {
      return InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      throw new RuntimeException(e);
    }
  }

  public static String userName() {
    return System.getProperty("user.name");
  }
}