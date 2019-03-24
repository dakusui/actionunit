package com.github.dakusui.actionunit.ut.actions.cmd.linux;

import com.github.dakusui.actionunit.actions.cmd.linux.Mkdir;
import com.github.dakusui.actionunit.core.context.ContextFunctions;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import static com.github.dakusui.crest.Crest.asObject;
import static com.github.dakusui.crest.Crest.assertThat;

public class MkdirTest {
  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
  private final List<String> out = new LinkedList<>();

  @Test
  public void test1() throws IOException {
    File target = new File(createTempDir(), "testdir");
    performAsAction(
        newMkdir()
            .dir(target));
    assertThat(
        target,
        asObject("exists").equalTo(true).$()
    );
  }

  @Test
  public void test2() throws IOException {
    File target = new File(createTempDir(), "testdir");
    performAsAction(
        newMkdir()
            .dir(target.getAbsolutePath()));
    assertThat(
        target,
        asObject("exists").equalTo(true).$()
    );
  }

  @Test
  public void test3() throws IOException {
    File target = new File(new File(createTempDir(), "testdir"), "child");
    performAsAction(
        newMkdir()
            .recursive()
            .dir(target.getAbsolutePath()));
    assertThat(
        target,
        asObject("exists").equalTo(true).$()
    );
  }


  private void performAsAction(Mkdir echo) {
    ReportingActionPerformer.create().performAndReport(
        echo.downstreamConsumer(downstreamConsumer())
            .toAction(),
        Writer.Std.OUT
    );
  }

  private Consumer<String> downstreamConsumer() {
    return ((Consumer<String>) System.out::println).andThen(out::add);
  }

  private Mkdir newMkdir() {
    return new Mkdir(ContextFunctions.DEFAULT_PLACE_HOLDER_FORMATTER);
  }

  private File createTempDir() throws IOException {
    Path path = Files.createTempDirectory("cmd-test");
    File ret = path.toFile();
    ret.deleteOnExit();
    return ret;
  }
}
