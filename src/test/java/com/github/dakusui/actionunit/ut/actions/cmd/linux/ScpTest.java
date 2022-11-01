package com.github.dakusui.actionunit.ut.actions.cmd.linux;

import com.github.dakusui.actionunit.actions.cmd.CommandLineComposer;
import com.github.dakusui.actionunit.actions.cmd.unix.Scp;
import com.github.dakusui.actionunit.actions.cmd.unix.SshOptions;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.pcond.forms.Predicates;
import com.github.dakusui.pcond.forms.Printables;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.function.Function;

import static com.github.dakusui.pcond.TestAssertions.assertThat;
import static com.github.dakusui.pcond.forms.Predicates.*;

public class ScpTest extends CommanderTestBase {
  @Test
  @Ignore
  public void test1() throws IOException {
    createNewDir("hello");
    createNewFile("world");
    performAction(
        newScp()
            .recursive()
            .file(Scp.Target.of(absolutePathOf("hello")))
            .file(Scp.Target.of(absolutePathOf("world")))
            .to(Scp.Target.of("ngsuser", "testenv101", "~/tmpdir")).toAction()
    );
  }

  @Test
  @Ignore
  public void test3() {
    Scp scp = newScp().sshOptionsResolver(h -> new SshOptions.Builder()
            .disablePasswordAuthentication()
            .disableStrictHostkeyChecking()
            .identity("~/.ssh/id_rsa").build())
        .to(Scp.Target.of("hello"));
    System.out.printf("%s%n", scp.toAction());
    performAction(scp.toAction());
  }

  @Test
  public void givenScpWithOptions$whenBuildCommandLineComposerMultipleTimes$thenCommandLineStringsAreAllCorrect() {
    Scp scp = newScp()
        .sshOptionsResolver(
            h -> new SshOptions.Builder()
                .disablePasswordAuthentication()
                .disableStrictHostkeyChecking()
                .identity("~/.ssh/id_rsa")
                .build())
        .file(Scp.Target.of("user", "remotehost1", "/remote/path"))
        .to(Scp.Target.of("hello"));
    String expectedString = "scp -i ~/.ssh/id_rsa -o "
        + "PasswordAuthentication=no -o StrictHostkeyChecking=no "
        + "'(Target::format(user@remotehost1:/remote/path)) "
        + "'(Target::format(hello))";
    assertThat(
        scp.buildCommandLineComposer(),
        allOf(
            transform(commandLineComposer$format()).check(isEqualTo(expectedString)),
            transform(commandLineComposer$format()).check(isEqualTo(expectedString))));
    System.out.println("1:" + scp.buildCommandLineComposer().format());
    System.out.println("2:" + scp.buildCommandLineComposer().format());
    System.out.println("3:" + scp.buildCommandLineComposer().format());
  }

  @Test
  public void tryJumpHostOption() {
    Scp scp = newScp().sshOptionsResolver(h -> new SshOptions.Builder()
            .addJumpHost("alexios.local")
            .build())
        .file(Scp.Target.of("user", "remotehost1", "/remote/path"))
        .to(Scp.Target.of("hello"));
    String expectedString = "scp -J alexios.local 'user@remotehost1:/remote/path' 'hello'";
    System.out.println(scp.buildCommandLineComposer().compose(Context.create()));
    assertThat(
        scp.buildCommandLineComposer(),
        transform((CommandLineComposer c) -> c.compose(Context.create())).check(isEqualTo(expectedString)));
  }

  @Test
  public void givenScp$whenBuildCommandLineComposerMultipleTimes$thenCommandLineStringsAreAllCorrect() {
    Scp scp = newScp().file(Scp.Target.of("localfile")).to(Scp.Target.of("remotehost1", "hello"));
    String expectedString = "scp -o StrictHostkeyChecking=no -o PasswordAuthentication=no "
        + "'(Target::format(localfile)) "
        + "'(Target::format(remotehost1:hello))";
    try {
      assertThat(
          scp.buildCommandLineComposer(),
          allOf(
              transform(commandLineComposer$format()).check(isEqualTo(expectedString)),
              transform(commandLineComposer$format()).check(Predicates.equalTo(expectedString))));
    } finally {
      System.out.println("expected: <" + expectedString + ">");
      System.out.println("1:        <" + scp.buildCommandLineComposer().format() + ">");
      System.out.println("2:" + scp.buildCommandLineComposer().format());
      System.out.println("3:" + scp.buildCommandLineComposer().format());
    }
  }

  @Test
  public void givenScpWithCustomSshOptions$whenBuildCommandLineComposerMultipleTimes$thenCommandLineStringsAreAllCorrect() {
    Scp scp = newScp().file(Scp.Target.of("localfile")).to(Scp.Target.of("remotehost1", "hello"));
    String expectedString = "scp -o StrictHostkeyChecking=no -o PasswordAuthentication=no "
        + "'(Target::format(localfile)) "
        + "'(Target::format(remotehost1:hello))";
    try {
      assertThat(
          scp.buildCommandLineComposer(),
          allOf(
              transform(commandLineComposer$format()).check(Predicates.equalTo(expectedString)),
              transform(commandLineComposer$format()).check(Predicates.equalTo(expectedString))));
    } finally {
      System.out.println("expected:" + expectedString);
      System.out.println("1:" + scp.buildCommandLineComposer().format());
      System.out.println("2:" + scp.buildCommandLineComposer().format());
      System.out.println("3:" + scp.buildCommandLineComposer().format());
    }
  }

  private static Function<CommandLineComposer, String> commandLineComposer$format() {
    return Printables.function("CommandLineComposer::format", CommandLineComposer::format);
  }

  private Scp newScp() {
    return scp();
  }
}
