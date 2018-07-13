package com.github.dakusui.actionunit.extras.cmdaction.linux;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.extras.cmd.linux.Ls;
import com.github.dakusui.actionunit.extras.cmd.linux.Mkdir;
import com.github.dakusui.actionunit.extras.cmd.linux.Rm;
import com.github.dakusui.actionunit.extras.cmd.linux.Touch;
import com.github.dakusui.actionunit.extras.cmdaction.FsTestBase;
import com.github.dakusui.cmd.exceptions.UnexpectedExitValueException;
import org.junit.Test;

import java.io.IOException;
import java.util.function.Function;

import static com.github.dakusui.crest.Crest.*;
import static java.util.Collections.singletonList;

public class RmTest extends FsTestBase<Rm> {
  public RmTest() throws IOException {
  }

  @Test
  public void removeNormalFile() {
    perform(
        this.context.sequential(
            this.commander.file(() -> "f").build(),
            this.configure(new Ls(this.context).cwd(dir).sortByMtime()).build()
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
        this.context.sequential(
            this.commander.file("g").build(),
            this.configure(new Ls(this.context).cwd(dir).sortByMtime()).build()
        )
    );
  }

  @Test
  public void removeDirectoryWithForceAndRecursiveOptions$thenFail() {
    perform(
        this.context.sequential(
            this.commander.file("g").force().recursive().build(),
            this.configure(new Ls(this.context).cwd(dir).sortByMtime()).build()
        )
    );
    assertThat(
        this.stdout,
        allOf(
            asListOf(String.class).equalTo(singletonList("f")).$()
        ));
  }

  @Override
  protected Rm create(Context context) {
    return new Rm(context);
  }

  @Override
  protected Function<Context, Action> preparation() {
    return $ -> $.sequential(
        new Touch($).cwd(dir).file("f").build(),
        new Mkdir($).cwd(dir).dir("g").build(),
        new Touch($).cwd(dir).file("g/h").build()
    );
  }
}
