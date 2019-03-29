package com.github.dakusui.actionunit.ut.actions.cmd.linux;

import com.github.dakusui.actionunit.actions.cmd.linux.Scp;
import com.github.dakusui.actionunit.actions.cmd.linux.SshOptions;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static com.github.dakusui.crest.Crest.*;

public class ScpTest extends CommanderTestBase {
  @Test
  @Ignore
  public void test1() throws IOException {
    createNewDir("hello");
    createNewFile("world");
    performAction(
        newScp()
            .recursive()
            .file(Scp.Target.create(absolutePathOf("hello")))
            .file(Scp.Target.create(absolutePathOf("world")))
            .to(Scp.Target.create("ngsuser", "testenv101", "~/tmpdir")).toAction()
    );
  }

  @Test
  @Ignore
  public void test3() {
    Scp scp = newScp().options(
        new SshOptions.Builder().disablePasswordAuthentication().disableStrictHostkeyChecking().identity("~/.ssh/id_rsa").build()).to(Scp.Target.create("hello"));
    System.out.println(String.format("%s", scp.toAction()));
    performAction(scp.toAction());
  }

  @Test
  public void givenScpWithOptions$whenBuildCommandLineComposerMultipleTimes$thenCommandLineStringsAreAllCorrect() {
    Scp scp = newScp()
        .options(
            new SshOptions.Builder()
                .disablePasswordAuthentication()
                .disableStrictHostkeyChecking()
                .identity("~/.ssh/id_rsa").build())
        .to(Scp.Target.create("hello"));
    assertThat(
        scp,
        allOf(
            asString(call("buildCommandLineComposer").andThen("commandLineString").$())
                .startsWith("scp -i ~/.ssh/id_rsa -o PasswordAuthentication=no -o StrictHostkeyChecking=no")
                .containsString("hello")
                .$(),
            asString(call("buildCommandLineComposer").andThen("commandLineString").$())
                .startsWith("scp -i ~/.ssh/id_rsa -o PasswordAuthentication=no -o StrictHostkeyChecking=no")
                .containsString("hello")
                .$())
    );
    System.out.println("1:" + scp.buildCommandLineComposer().commandLineString());
    System.out.println("2:" + scp.buildCommandLineComposer().commandLineString());
    System.out.println("3:" + scp.buildCommandLineComposer().commandLineString());
  }

  @Test
  public void givenScp$whenBuildCommandLineComposerMultipleTimes$thenCommandLineStringsAreAllCorrect() {
    Scp scp = newScp().to(Scp.Target.create("hello"));
    assertThat(
        scp,
        allOf(
            asString(call("buildCommandLineComposer").andThen("commandLineString").$())
                .startsWith("scp ")
                .containsString("hello")
                .$(),
            asString(call("buildCommandLineComposer").andThen("commandLineString").$())
                .startsWith("scp ")
                .containsString("hello")
                .$())
    );
  }

  private Scp newScp() {
    return scp();
  }
}
