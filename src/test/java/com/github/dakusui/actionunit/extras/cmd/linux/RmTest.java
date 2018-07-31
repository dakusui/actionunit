package com.github.dakusui.actionunit.extras.cmd.linux;

import com.github.dakusui.actionunit.extras.cmd.FsTestBase;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.linux.Ls;
import com.github.dakusui.actionunit.linux.Mkdir;
import com.github.dakusui.actionunit.linux.Rm;
import com.github.dakusui.actionunit.linux.Touch;
import com.github.dakusui.cmd.exceptions.UnexpectedExitValueException;
import org.junit.Test;

import java.io.IOException;

import static com.github.dakusui.actionunit.ut.utils.TestUtils.isRunUnderLinux;
import static com.github.dakusui.actionunit.core.ActionSupport.sequential;
import static com.github.dakusui.crest.Crest.*;
import static java.util.Collections.singletonList;
import static org.junit.Assume.assumeTrue;

public class RmTest extends FsTestBase<Rm> {
  public RmTest() throws IOException {
  }

  @Test
  public void removeNormalFile() {
    perform(
        sequential(
            this.commander.file((context) -> "f").build(),
            this.configure(new Ls().cwd(dir).sortByMtime()).build()
        )
    );
    assertThat(
        this.stdout,
        allOf(
            asListOf(String.class).equalTo(singletonList("g")).$()
        ));
  }


  @Test(expected = UnexpectedExitValueException.class)
  public void tryToRemoveDirectory$thenFail() {
    perform(
        sequential(
            this.commander.file("g").build(),
            this.configure(new Ls().cwd(dir).sortByMtime()).build()
        )
    );
  }

  @Test
  public void removeDirectoryWithForceAndRecursiveOptions$thenFail() {
    assumeTrue(isRunUnderLinux());
    perform(
        sequential(
            this.commander.file("g").force().recursive().build(),
            this.configure(new Ls().cwd(dir).sortByMtime()).build()
        )
    );
    assertThat(
        this.stdout,
        allOf(
            asListOf(String.class).equalTo(singletonList("f")).$()
        ));
  }

  @Override
  protected Rm create() {
    return new Rm();
  }

  @Override
  protected Action preparation() {
    return sequential(
        new Touch().cwd(dir).file("f").build(),
        new Mkdir().cwd(dir).dir("g").build(),
        new Touch().cwd(dir).file("g/h").build()
    );
  }
}
