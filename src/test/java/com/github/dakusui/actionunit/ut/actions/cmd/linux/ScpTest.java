package com.github.dakusui.actionunit.ut.actions.cmd.linux;

import com.github.dakusui.actionunit.actions.cmd.unix.Scp;
import com.github.dakusui.actionunit.actions.cmd.unix.SshOptions;
import com.github.dakusui.actionunit.core.Context;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static com.github.dakusui.crest.Crest.allOf;
import static com.github.dakusui.crest.Crest.asString;
import static com.github.dakusui.crest.Crest.assertThat;
import static com.github.dakusui.crest.Crest.call;

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
    Scp scp = newScp().options(
        new SshOptions.Builder().disablePasswordAuthentication().disableStrictHostkeyChecking().identity("~/.ssh/id_rsa").build()).to(Scp.Target.of("hello"));
    System.out.printf("%s%n", scp.toAction());
    performAction(scp.toAction());
  }

  @Test
  public void givenScpWithOptions$whenBuildCommandLineComposerMultipleTimes$thenCommandLineStringsAreAllCorrect() {
    Scp scp = newScp()
        .options(
            new SshOptions.Builder()
                .disablePasswordAuthentication()
                .disableStrictHostkeyChecking()
                .identity("~/.ssh/id_rsa")
                .build())
        .file(Scp.Target.of("user", "remotehost1", "/remote/path"))
        .to(Scp.Target.of("hello"));
    String expectedString = "scp -i ~/.ssh/id_rsa -o "
        + "PasswordAuthentication=no -o StrictHostkeyChecking=no "
        + "quoteWith['](Target::format(user@remotehost1:/remote/path)) "
        + "quoteWith['](Target::format(hello))";
    assertThat(
        scp.buildCommandLineComposer(),
        allOf(
            asString(call("format").$()).equalTo(expectedString).$(),
            asString(call("format").$()).equalTo(expectedString).$()));
    System.out.println("1:" + scp.buildCommandLineComposer().format());
    System.out.println("2:" + scp.buildCommandLineComposer().format());
    System.out.println("3:" + scp.buildCommandLineComposer().format());
  }

  @Test
  public void tryJumpHostOption() {
    Scp scp = newScp().options(new SshOptions.Builder()
            .addJumpHost("alexios.local")
            .build())
        .file(Scp.Target.of("user", "remotehost1", "/remote/path"))
        .to(Scp.Target.of("hello"));
    String expectedString = "scp -J alexios.local 'user@remotehost1:/remote/path' 'hello'";
    System.out.println(scp.buildCommandLineComposer().compose(Context.create()));
    assertThat(
        scp.buildCommandLineComposer(),
        asString(call("compose", Context.create()).$()).equalTo(expectedString).$());
  }

  @Test
  public void givenScp$whenBuildCommandLineComposerMultipleTimes$thenCommandLineStringsAreAllCorrect() {
    Scp scp = newScp().file(Scp.Target.of("localfile")).to(Scp.Target.of("remotehost1", "hello"));
    String expectedString = "scp -o StrictHostkeyChecking=no -o PasswordAuthentication=no "
        + "quoteWith['](Target::format(localfile)) "
        + "quoteWith['](Target::format(remotehost1:hello))";
    assertThat(
        scp.buildCommandLineComposer(),
        allOf(
            asString(call("format").$()).equalTo(expectedString).$(),
            asString(call("format").$()).equalTo(expectedString).$()));
    System.out.println("1:" + scp.buildCommandLineComposer().format());
    System.out.println("2:" + scp.buildCommandLineComposer().format());
    System.out.println("3:" + scp.buildCommandLineComposer().format());
  }

  @Test
  public void givenScpWithCustomSshOptions$whenBuildCommandLineComposerMultipleTimes$thenCommandLineStringsAreAllCorrect() {
    Scp scp = newScp().file(Scp.Target.of("localfile")).to(Scp.Target.of("remotehost1", "hello"));
    String expectedString = "scp -o StrictHostkeyChecking=no -o PasswordAuthentication=no "
        + "quoteWith['](Target::format(localfile)) "
        + "quoteWith['](Target::format(remotehost1:hello))";
    assertThat(
        scp.buildCommandLineComposer(),
        allOf(
            asString(call("format").$()).equalTo(expectedString).$(),
            asString(call("format").$()).equalTo(expectedString).$()));
    System.out.println("1:" + scp.buildCommandLineComposer().format());
    System.out.println("2:" + scp.buildCommandLineComposer().format());
    System.out.println("3:" + scp.buildCommandLineComposer().format());
  }

  private Scp newScp() {
    return scp();
  }
}
