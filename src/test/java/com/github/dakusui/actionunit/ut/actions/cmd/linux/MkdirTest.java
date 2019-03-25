package com.github.dakusui.actionunit.ut.actions.cmd.linux;

import com.github.dakusui.actionunit.actions.cmd.linux.Mkdir;
import com.github.dakusui.actionunit.core.context.ContextFunctions;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.github.dakusui.actionunit.core.context.ContextFunctions.immediateOf;
import static com.github.dakusui.crest.Crest.asObject;
import static com.github.dakusui.crest.Crest.assertThat;

public class MkdirTest extends CommanderTestBase {
  @Test
  public void whenMkdirUsingFile$thenDirectoryCreated() {
    File target = new File(this.baseDir(), "testdir");
    performAsAction(
        newMkdir()
            .dir(target));
    assertThat(
        target,
        asObject("exists").equalTo(true).$()
    );
  }

  @Test
  public void whenMkdirUsingContextFunction$thenDirectoryCreated() {
    File target = new File(this.baseDir(), "testdir");
    performAsAction(
        newMkdir()
            .dir(immediateOf(target.getAbsolutePath())));
    assertThat(
        target,
        asObject("exists").equalTo(true).$()
    );
  }

  @Test
  public void whenMkdirUsingString$thenDirectoryCreated() {
    File target = new File(this.baseDir(), "testdir");
    performAsAction(
        newMkdir()
            .dir(target.getAbsolutePath()));
    assertThat(
        target,
        asObject("exists").equalTo(true).$()
    );
  }

  @Test
  public void whenMkdirRecursivelyNestedDirectory$thenDirectoryCreated() {
    File target = new File(new File(this.baseDir(), "testdir"), "child");
    performAsAction(
        newMkdir()
            .recursive()
            .dir(target.getAbsolutePath()));
    assertThat(
        target,
        asObject("exists").equalTo(true).$()
    );
  }


  private Mkdir newMkdir() {
    return mkdir();
  }
}
