package com.github.dakusui.actionunit.ut.actions.cmd;

import com.github.dakusui.actionunit.actions.cmd.CommanderConfig;
import com.github.dakusui.actionunit.actions.cmd.CommanderFactoryManager;
import com.github.dakusui.actionunit.actions.cmd.unix.Echo;
import com.github.dakusui.actionunit.actions.cmd.unix.Scp;
import com.github.dakusui.actionunit.actions.cmd.unix.SshOptions;
import com.github.dakusui.actionunit.actions.cmd.unix.SshShell;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import com.github.dakusui.pcond.TestAssertions;
import com.github.dakusui.pcond.forms.Predicates;
import com.github.dakusui.pcond.forms.Printables;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.dakusui.crest.Crest.*;
import static com.github.dakusui.pcond.forms.Predicates.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

@RunWith(Enclosed.class)
public class CommanderFactoryManagerTest {
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
      TestAssertions.assertThat(
          localEcho(),
          Predicates.allOf(
              Predicates.transform(buildCommandLineComposeAndThenCompose(Context.create())).check(findRegexes("echo", "'hello world'$")),
              Predicates.transform(shellAndThenFormat()).check(findRegexes("sh", "-c$")))
      );
    }

    private Function<Echo, String> shellAndThenFormat() {
      return (Echo v) -> v.shell().format();
    }

    private Function<Echo, String> buildCommandLineComposeAndThenCompose(Context context) {
      return Printables.function("buildCommandLineComposerAndThenCompose", (Echo v) -> v.buildCommandLineComposer().compose(context));
    }

    @Test
    public void formatRemoteEcho() {
      TestAssertions.assertThat(
          remoteEcho(),
          Predicates.allOf(
              Predicates.transform(buildCommandLineComposeAndThenCompose(Context.create())).check(findRegexes("echo", "'hello world'$")),
              Predicates.transform(shellAndThenFormat()).check(substringAfterExpectedRegexesForSshOptions_())));
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


    abstract Function<String, String> substringAfterExpectedRegexesForSshOptions_Scp();

    abstract Predicate<? super String> substringAfterExpectedRegexesForSshOptions_();

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
                  new SshShell.Builder(host, createDefaultSshOptionsInCommandFactoryManagerTest()
                      .disableStrictHostkeyChecking()
                      .disablePasswordAuthentication())
                      .program("ssh")
                      .enableAuthAgentConnectionForwarding()
                      .build())
              .build();
    }

    @Override
    public Predicate<String> substringAfterExpectedRegexesForSshOptions_() {
      return findSubstrings("ssh", "-A", "-o StrictHostkeyChecking=no", "-o PasswordAuthentication=no", hostName());
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
              .shell(new SshShell.Builder(host, createSshOptionsBuilder())
                  .program("ssh")
                  .user(userName())
                  .enableAuthAgentConnectionForwarding()
                  .build())
              .build();
    }

    private static SshOptions.Builder createSshOptionsBuilder() {
      return createDefaultSshOptionsInCommandFactoryManagerTest()
          .disableStrictHostkeyChecking()
          .disablePasswordAuthentication();
    }


    public Predicate<String> substringAfterExpectedRegexesForSshOptions_() {
      return findSubstrings("ssh", "-A", "-o StrictHostkeyChecking=no", "-o PasswordAuthentication=no", String.format("%s@%s", userName(), hostName()));
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
          CommanderConfig.builder()
              .sshOptions(createDefaultSshOptionsInCommandFactoryManagerTest().build())
              .build() :
          CommanderConfig.builder()
              .shell(new SshShell.Builder(host, createDefaultSshOptionsInCommandFactoryManagerTest())
                  .program("ssh")
                  .user(userName())
                  .enableAuthAgentConnectionForwarding()
                  .build())
              .build();
    }

    @Override
    public Predicate<String> substringAfterExpectedRegexesForSshOptions_() {
      return findSubstrings("ssh", "-A", "-4", "-F", "-p 9999", "-v", String.format("%s@%s", userName(), hostName()));
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
              .user(userName())
              .enableAuthAgentConnectionForwarding()
              .build()).build();
    }

    @Override
    public Predicate<String> substringAfterExpectedRegexesForSshOptions_() {
      return findSubstrings("ssh", "-A", "-6", "-c cipher_spec", "-i id_rsa", "-q", String.format("%s@%s", userName(), hostName()));
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

  private static SshOptions.Builder createDefaultSshOptionsInCommandFactoryManagerTest() {
    return new SshOptions.Builder()
        .ipv4(true)
        .ipv6(false)
        .compression(true)
        .cipherSpec(null)
        .configFile("ssh_config")
        .identity(null)
        .port(9999)
        .quiet(false)
        .verbose(true);
  }
}