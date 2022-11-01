package com.github.dakusui.actionunit.ut.actions.cmd;

import com.github.dakusui.actionunit.actions.ContextVariable;
import com.github.dakusui.actionunit.actions.cmd.*;
import com.github.dakusui.actionunit.actions.cmd.unix.Echo;
import com.github.dakusui.actionunit.actions.cmd.unix.Scp;
import com.github.dakusui.actionunit.actions.cmd.unix.SshOptions;
import com.github.dakusui.actionunit.actions.cmd.unix.SshShell;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
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
      perform(echo(configFor("localhost")).toAction());
    }

    @Test
    public void performRemoteEcho() {
      assumeThat(isRunOnLinux(), isTrue());
      perform(remoteEcho(hostName()).toAction());
    }

    @Test
    public void composeLocalEchoCommandLine() {
      String h = "127.0.0.1";
      assertThat(
          echo(configFor(h)),
          allOf(
              transform(composeEchoCommandLine())
                  .check(findRegexes("echo", "'hello world'$")),
              transform(composeShellCommandLineOfEchoCommand())
                  .check(findRegexes("sh", "-c$")))
      );
    }

    @Test
    public void composeRemoteEchoCommandLine() {
      String h = hostName();
      Echo remoteEcho = remoteEcho(h);
      assertThat(
          remoteEcho,
          allOf(
              transform(composeEchoCommandLine()).check(findRegexes("echo", "'hello world'$")),
              transform(composeShellCommandLineOfEchoCommand()).check(substringAfterExpectedRegexesForSshOptions())));
    }

    @Test
    public void composeLocalScpCommandLine() {
      String h = "localhost";
      Scp scp = scp(configFor(h));
      System.out.println(scp.buildCommandLineComposer().apply(new ContextVariable[0]).apply(Context.create(), new Object[0]));
      assertThat(
          scp,
          transform(scpBuildCommandLineComposerAndThenCompose())
              .check(substringAfterExpectedRegexesForSshOptions_Scp()));
    }

    abstract Predicate<String> substringAfterExpectedRegexesForSshOptions_Scp();

    abstract Predicate<String> substringAfterExpectedRegexesForSshOptions();

    abstract UnixCommanderFactoryManager createUnixCommanderFactoryManager(CommanderConfig config);

    abstract CommanderConfig configForRemote(String host);

    abstract CommanderConfig configForLocal();

    final CommanderConfig configFor(String host) {
      if ("localhost".equals(host))
        return configForLocal();
      return configForRemote(host);
    }

    final UnixCommanderFactoryManager factoryManager(CommanderConfig commanderConfig) {
      return createUnixCommanderFactoryManager(commanderConfig);
    }


    private Echo echo(CommanderConfig commanderConfig) {
      return factoryManager(commanderConfig).local().echo().message("hello world").downstreamConsumer(System.out::println);
    }

    private Echo remoteEcho(String host) {
      return UnixCommanderFactory.create(ShellManager.createShellManager(), host)
          .echo()
          .message("hello world")
          .downstreamConsumer(System.out::println);
    }

    private Scp scp(CommanderConfig commanderConfig) {
      return factoryManager(commanderConfig).local()
          .scp().file(Scp.Target.of("/local/file"))
          .to(Scp.Target.of("user", "host", "/remote/file"));
    }

    private static void perform(Action action) {
      ReportingActionPerformer.create().perform(action);
    }

    private static Function<? super Echo, String> composeShellCommandLineOfEchoCommand() {
      return shellAndThenFormat();
    }

    private static <T extends Commander<T>> Function<? super T, String> shellAndThenFormat() {
      return function("shellAndThenFormat", (T v) -> v.shell().format());
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
    CommanderConfig configForRemote(String host) {
      return CommanderConfig.builder().shell(
              new SshShell.Builder(
                  host,
                  createDefaultSshOptionsInCommandFactoryManagerTest()
                      .authAgentConnectionForwarding(true)
                      .disableStrictHostkeyChecking()
                      .disablePasswordAuthentication()
                      .build())
                  .program("ssh")
                  .build())
          .build();
    }

    @Override
    CommanderConfig configForLocal() {
      return CommanderConfig.DEFAULT;
    }

    @Override
    public Predicate<String> substringAfterExpectedRegexesForSshOptions() {
      return findSubstrings(
          "ssh", "-A",
          "-o StrictHostkeyChecking=no",
          "-o PasswordAuthentication=no",
          hostName());
    }

    @Override
    UnixCommanderFactoryManager createUnixCommanderFactoryManager(CommanderConfig config) {
      return new UnixCommanderFactoryManager.Builder()
          .localCommanderFactory(m -> new UnixCommanderFactory.Builder().build())
          .remoteCommanderFactory((m, h) -> new UnixCommanderFactory.Builder().config(configFor(h)).build())
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
    @Override
    CommanderConfig configForLocal() {
      return CommanderConfig.DEFAULT;
    }

    CommanderConfig configForRemote(String host) {
      return CommanderConfig.builder()
          .shell(new SshShell.Builder(host,
              createSshOptionsBuilder()
                  .authAgentConnectionForwarding(true)
                  .build())
              .program("ssh")
              .user(userName())
              .build())
          .build();
    }

    private static SshOptions.Builder createSshOptionsBuilder() {
      return createDefaultSshOptionsInCommandFactoryManagerTest()
          .disableStrictHostkeyChecking()
          .disablePasswordAuthentication();
    }


    public Predicate<String> substringAfterExpectedRegexesForSshOptions() {
      return findSubstrings("ssh", "-A", "-o StrictHostkeyChecking=no", "-o PasswordAuthentication=no", String.format("%s@%s", userName(), hostName()));
    }

    @Override
    UnixCommanderFactoryManager createUnixCommanderFactoryManager(CommanderConfig config) {
      return new UnixCommanderFactoryManager.Builder()
          .localCommanderFactory(m -> new UnixCommanderFactory.Builder().build())
          .remoteCommanderFactory((m, h) -> new UnixCommanderFactory.Builder().config(configFor(h)).build())
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
    CommanderConfig configForRemote(String host) {
      return CommanderConfig.builder()
          .shell(new SshShell.Builder(
              host,
              createDefaultSshOptionsInCommandFactoryManagerTest()
                  .authAgentConnectionForwarding(true)
                  .build())
              .program("ssh")
              .user(userName())
              .build())
          .build();
    }

    @Override
    CommanderConfig configForLocal() {
      return CommanderConfig.builder().build();
    }

    @Override
    public Predicate<String> substringAfterExpectedRegexesForSshOptions() {
      return findSubstrings("ssh",
          "-A",
          "-4", "-F", "-p 22",
          "-v",
          String.format("%s@%s", userName(), hostName()));
    }

    @Override
    UnixCommanderFactoryManager createUnixCommanderFactoryManager(CommanderConfig config) {
      return new UnixCommanderFactoryManager.Builder()
          .localCommanderFactory(m -> new UnixCommanderFactory.Builder().build())
          .remoteCommanderFactory((m, h) -> new UnixCommanderFactory.Builder().config(config).build())
          .build();
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

    private final SshOptions sshOptions = new SshOptions.Impl(
        true, false, true, false,
        asList("jumphost1", "jumphost2"),
        "cipher_spec", null, "id_rsa",
        emptyList(),
        null, true, false);

    CommanderConfig configForRemote(String host) {
      return CommanderConfig.builder().shell(new SshShell.Builder(host, sshOptions)
          .program("ssh")
          .user(userName())
          .build()).build();
    }

    @Override
    CommanderConfig configForLocal() {
      return CommanderConfig.builder().build();
    }

    @Override
    public Predicate<String> substringAfterExpectedRegexesForSshOptions() {
      return findSubstrings("ssh", "-A", "-6", "-c cipher_spec", "-i id_rsa", "-q", String.format("%s@%s", userName(), hostName()));
    }

    @Override
    UnixCommanderFactoryManager createUnixCommanderFactoryManager(CommanderConfig config) {
      return new UnixCommanderFactoryManager.Builder()
          .localCommanderFactory(m -> new UnixCommanderFactory.Builder().build())
          .remoteCommanderFactory((m, h) -> new UnixCommanderFactory.Builder().config(configFor(h)).build())
          .build();
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