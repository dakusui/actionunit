package com.github.dakusui.actionunit.ut.actions.cmd.linux;

import com.github.dakusui.actionunit.actions.cmd.linux.Scp;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

public class ScpTest extends CommanderTestBase {
  @Ignore
  @Test
  public void test() throws IOException {
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


  private Scp newScp() {
    return scp();
  }
}
