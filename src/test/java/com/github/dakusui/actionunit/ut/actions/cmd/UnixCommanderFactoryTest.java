package com.github.dakusui.actionunit.ut.actions.cmd;

import com.github.dakusui.actionunit.actions.ContextVariable;
import com.github.dakusui.actionunit.actions.cmd.Commander;
import com.github.dakusui.actionunit.actions.cmd.ShellManager;
import com.github.dakusui.actionunit.actions.cmd.UnixCommanderFactory;
import com.github.dakusui.actionunit.actions.cmd.unix.Echo;
import com.github.dakusui.actionunit.actions.cmd.unix.Scp;
import com.github.dakusui.actionunit.actions.cmd.unix.SshOptions;
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
import java.util.function.Predicate;

import static com.github.dakusui.actionunit.ut.utils.TestUtils.isRunOnLinux;
import static com.github.dakusui.pcond.TestAssertions.assertThat;
import static com.github.dakusui.pcond.TestAssertions.assumeThat;
import static com.github.dakusui.pcond.forms.Predicates.*;
import static com.github.dakusui.pcond.forms.Printables.function;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

@RunWith(Enclosed.class)
public class UnixCommanderFactoryTest {
  public static abstract class Base {
    public Base() {
    }

    @Test
    public void performLocalEcho() {
      assumeThat(isRunOnLinux(), isTrue());
      perform(echo(createShellManager()).toAction());
    }

    @Ignore
    @Test
    public void performRemoteEcho() {
      assumeThat(isRunOnLinux(), isTrue());
      perform(echo(createShellManager()).toAction());
    }

    abstract SshOptions createSshOptions();

    @Test
    public void composeLocalEchoCommandLine() {
      assertThat(
          echo(createShellManager()),
          allOf(
              transform(composeEchoCommandLine())
                  .check(findRegexes("echo", "'hello world'$")),
              transform(composeShellCommandLineOfEchoCommand("localhost"))
                  .check(findRegexes("sh", "-c$")))
      );
    }

    private ShellManager createShellManager() {
      return ShellManager.createShellManager(h -> createSshOptions());
    }

    @Test
    public void composeRemoteEchoCommandLine() {
      assertThat(
          echo(createShellManager()),
          allOf(
              transform(composeEchoCommandLine()).check(findRegexes("echo", "'hello world'$")),
              transform(composeShellCommandLineOfEchoCommand(hostName())).check(substringAfterExpectedRegexesForSshOptions())));
    }

    @Test
    public void composeLocalScpCommandLine() {
      Scp scp = scp(createShellManager());
      System.out.println(scp.buildCommandLineComposer().apply(new ContextVariable[0]).apply(Context.create(), new Object[0]));
      assertThat(
          scp,
          transform(scpBuildCommandLineComposerAndThenCompose())
              .check(substringAfterExpectedRegexesForSshOptions_Scp()));
    }

    abstract Predicate<String> substringAfterExpectedRegexesForSshOptions_Scp();

    abstract Predicate<String> substringAfterExpectedRegexesForSshOptions();

    private Echo echo(ShellManager shellManager) {
      return UnixCommanderFactory.create(shellManager)
          .echo()
          .message("hello world")
          .downstreamConsumer(System.out::println);
    }

    private Scp scp(ShellManager shellManager) {
      return UnixCommanderFactory.create(shellManager)
          .scp().file(Scp.Target.of("/local/file"))
          .to(Scp.Target.of("user", "host", "/remote/file"));
    }

    private static void perform(Action action) {
      ReportingActionPerformer.create().perform(action);
    }

    private static Function<? super Echo, String> composeShellCommandLineOfEchoCommand(String host) {
      return shellAndThenFormat(host);
    }

    private static <T extends Commander<T>> Function<? super T, String> shellAndThenFormat(String host) {
      return function("shellAndThenFormat", (T v) -> v.shellManager().shellFor(host).format());
    }

    private static Function<? super Echo, String> composeEchoCommandLine() {
      return buildCommandLineComposerAndThenCompose(Context.create());
    }

    private static Function<? super Scp, String> scpBuildCommandLineComposerAndThenCompose() {
      return buildCommandLineComposerAndThenCompose(Context.create());
    }

    private static <T extends Commander<T>> Function<? super T, String> buildCommandLineComposerAndThenCompose(Context context) {
      return function("buildCommandLineComposerAndThenCompose", (T v) -> v.buildCommandLineComposer().compose(context));
    }
  }

  public static class WithoutUsername extends Base {
    @Override
    public Predicate<String> substringAfterExpectedRegexesForSshOptions() {
      return findSubstrings(
          "ssh", "-A",
          "-o StrictHostkeyChecking=no",
          "-o PasswordAuthentication=no",
          hostName());
    }

    @Override
    SshOptions createSshOptions() {
      return new SshOptions.Builder()
          .authAgentConnectionForwarding(true)
          .disableStrictHostkeyChecking()
          .disablePasswordAuthentication()
          .build();
    }

    @Override
    Predicate<String> substringAfterExpectedRegexesForSshOptions_Scp() {
      return findSubstrings("scp",
          "-o", "StrictHostkeyChecking=no",
          "-o", "PasswordAuthentication=no",
          "'/local/file'", "'user@host:/remote/file'");
    }
  }

  public static class WithUsername extends Base {
    public Predicate<String> substringAfterExpectedRegexesForSshOptions() {
      return findSubstrings("ssh", "-A", "-o StrictHostkeyChecking=no", "-o PasswordAuthentication=no", String.format("%s@%s", userName(), hostName()));
    }

    @Override
    SshOptions createSshOptions() {
      return createDefaultSshOptionsInCommandFactoryManagerTest()
          .disableStrictHostkeyChecking()
          .disablePasswordAuthentication()
          .build();
    }

    @Override
    Predicate<String> substringAfterExpectedRegexesForSshOptions_Scp() {
      return findSubstrings("scp",
          "-o", "StrictHostkeyChecking=no",
          "-o", "PasswordAuthentication=no",
          "'/local/file'", "'user@host:/remote/file'");
    }
  }

  public static class WithCustomSshOptions1 extends Base {
    @Override
    public Predicate<String> substringAfterExpectedRegexesForSshOptions() {
      return findSubstrings("ssh",
          "-A",
          "-4", "-F", "-p 22",
          "-v",
          String.format("%s@%s", userName(), hostName()));
    }

    @Override
    SshOptions createSshOptions() {
      return createDefaultSshOptionsInCommandFactoryManagerTest().build();
    }

    @Override
    Predicate<String> substringAfterExpectedRegexesForSshOptions_Scp() {
      return findSubstrings("scp",
          "-4", "-C", "-F ssh_config",
          "-P 22",
          "-v",
          "'/local/file'",
          "'user@host:/remote/file'");
    }
  }

  public static class WithCustomSshOptions2 extends Base {

    @Override
    public Predicate<String> substringAfterExpectedRegexesForSshOptions() {
      return findSubstrings("ssh", "-A", "-6", "-c cipher_spec", "-i id_rsa", "-q", String.format("%s@%s", userName(), hostName()));
    }

    @Override
    SshOptions createSshOptions() {
      return new SshOptions.Impl(
          true, false, true, false,
          asList("jumphost1", "jumphost2"),
          "cipher_spec", null, "id_rsa",
          emptyList(),
          null, true, false);
    }

    @Override
    Predicate<String> substringAfterExpectedRegexesForSshOptions_Scp() {
      return findSubstrings("scp",
          "-6", "-c cipher_spec", "-i id_rsa", "-q",
          "'/local/file'",
          "'user@host:/remote/file'");
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
        .authAgentConnectionForwarding(true)
        .ipv4(true)
        .ipv6(false)
        .compression(true)
        .cipherSpec(null)
        .configFile("ssh_config")
        .identity(null)
        .port(22)
        .quiet(false)
        .verbose(true);
  }
}