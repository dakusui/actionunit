package com.github.dakusui.actionunit.extras.cmd.linux;

import com.github.dakusui.actionunit.extras.cmd.FsTestBase;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.linux.Mkdir;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static com.github.dakusui.actionunit.ut.utils.TestUtils.isRunUnderLinux;
import static com.github.dakusui.actionunit.core.ActionSupport.sequential;
import static com.github.dakusui.actionunit.core.ActionSupport.simple;
import static com.github.dakusui.crest.Crest.*;
import static org.junit.Assume.assumeTrue;

public class MkdirTest extends FsTestBase<Mkdir> {

  public MkdirTest() throws IOException {
    super();
  }

  @Test
  public void mkdir() {
    perform(
        sequential(
            commander.dir("hello").recursive().build(),
            checkDirectoryExists(targetDir("hello"))
        ));
  }

  @Test
  public void mkdirWithFile() {
    perform(
        sequential(
            commander.dir(new File(this.dir, "hello")).recursive().build(),
            checkDirectoryExists(targetDir("hello"))
        ));
  }

  @Test
  public void mkdirP() {
    assumeTrue(isRunUnderLinux());
    perform(
        sequential(
            commander.cwd(this.dir).dir("hello/world").recursive().build(),
            checkDirectoryExists(targetDir("hello/world"))
        ));
  }

  private File targetDir(String relName) {
    return new File(dir.getAbsolutePath() + "/" + "hello");
  }

  private Action checkDirectoryExists(File file) {
    return simple(
        String.format("Check if a directory %s/ exists", file.getAbsolutePath()),
        (c) -> assertThat(
            file,
            allOf(
                asBoolean("exists").isTrue().$(),
                asBoolean("isDirectory").isTrue().$()
            )
        )
    );
  }

  @Override
  protected Mkdir create() {
    return new Mkdir();
  }

}
