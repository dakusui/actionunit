package com.github.dakusui.actionunit.extras.cmdaction.linux;

import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.extras.cmd.linux.Mkdir;
import com.github.dakusui.actionunit.extras.cmdaction.FsTestBase;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.function.Function;

import static com.github.dakusui.crest.Crest.*;

public class MkdirTest extends FsTestBase<Mkdir> {

  public MkdirTest() throws IOException {
    super();
  }

  @Test
  public void mkdir() {
    perform(
        context.sequential(
            commander.dir("hello").recursive().build(),
            checkDirectoryExists(targetDir("hello")).apply(context)
        ));
  }

  @Test
  public void mkdirWithFile() {
    perform(
        context.sequential(
            commander.dir(new File(this.dir, "hello")).recursive().build(),
            checkDirectoryExists(targetDir("hello")).apply(context)
        ));
  }

  @Test
  public void mkdirP() {
    perform(
        context.sequential(
            commander.cwd(this.dir).dir("hello/world").recursive().build(),
            checkDirectoryExists(targetDir("hello/world")).apply(context)
        ));
  }

  private File targetDir(String relName) {
    return new File(dir.getAbsolutePath() + "/" + "hello");
  }

  private Function<Context, Action> checkDirectoryExists(File file) {
    return context -> context.simple(
        String.format("Check if a directory %s/ exists", file.getAbsolutePath()),
        () -> assertThat(
            file,
            allOf(
                asBoolean("exists").isTrue().$(),
                asBoolean("isDirectory").isTrue().$()
            )
        )
    );
  }

  @Override
  protected Mkdir create(Context context) {
    return new Mkdir(context);
  }

}
