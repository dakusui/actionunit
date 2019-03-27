package com.github.dakusui.actionunit.ut.actions.cmd.linux;

import com.github.dakusui.actionunit.actions.cmd.linux.Scp;
import com.github.dakusui.actionunit.actions.cmd.linux.SshOptions;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

@Ignore

public class ScpTest extends CommanderTestBase {
  @Test
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
  public void test3() {
    Scp scp = newScp().options(
        new SshOptions.Builder().disablePasswordAuthentication().disableStrictHostkeyChecking().identity("~/.ssh/id_rsa").build()).to(Scp.Target.create("hello"));
    System.out.println(String.format("%s", scp.toAction()));
    performAction(scp.toAction());
  }


  private Scp newScp() {
    return scp();
  }
}
