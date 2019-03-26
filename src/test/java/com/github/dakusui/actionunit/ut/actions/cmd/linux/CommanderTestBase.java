package com.github.dakusui.actionunit.ut.actions.cmd.linux;

import com.github.dakusui.actionunit.actions.cmd.Cmd;
import com.github.dakusui.actionunit.actions.cmd.Commander;
import com.github.dakusui.actionunit.actions.cmd.CommanderFactory;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.core.context.ContextFunctions;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.ut.utils.TestUtils;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import static com.github.dakusui.crest.Crest.asBoolean;
import static com.github.dakusui.crest.Crest.requireThat;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public abstract class CommanderTestBase extends TestUtils.TestBase implements CommanderFactory {
  private       File         baseDir;
  private final List<String> out = new LinkedList<>();

  @Before
  public void setUp() throws IOException {
    this.baseDir = Files.createTempDirectory("cmd-test-").toFile();
  }

  @After
  public void tearDown() {
    performAction(cleanUp());
  }

  List<String> out() {
    return Collections.unmodifiableList(out);
  }

  void performAsAction(Commander commander) {
    this.performAction(initCommander(commander).toAction());
  }

  @SuppressWarnings("unchecked")
  <C extends Commander> C initCommander(C commander) {
    return (C) commander.downstreamConsumer(downstreamConsumer());
  }

  /**
   * Creates a temporary file which will be deleted on exit under a {@link CommanderTestBase#baseDir}.
   *
   * @param content of the file
   * @return A created file.
   * @throws IOException Failed to create the file.
   */
  File createTempFile(String... content) throws IOException {
    File file = File.createTempFile("cmd-test-", "tmp", baseDir);
    file.deleteOnExit();
    try (FileWriter writer = new FileWriter(file)) {
      for (String each : content) {
        writer.write(each);
        writer.write(format("%n"));
      }
    }
    return file;
  }

  @SuppressWarnings("UnusedReturnValue")
  File createNewFile(String name, String... content) throws IOException {
    File file = new File(this.baseDir, name);
    requireThat(file.createNewFile(), asBoolean().isTrue().$());
    file.deleteOnExit();
    try (FileWriter writer = new FileWriter(file)) {
      for (String each : content) {
        writer.write(each);
        writer.write(format("%n"));
      }
    }
    return file;
  }

  @SuppressWarnings("UnusedReturnValue")
  File createNewDir(@SuppressWarnings("SameParameterValue") String name) {
    File ret = new File(this.baseDir, name);
    requireThat(ret.mkdirs(), asBoolean().isTrue().$());
    return ret;
  }

  File baseDir() {
    return this.baseDir;
  }

  void performAction(Action action) {
    ReportingActionPerformer.create().performAndReport(action, Writer.Std.OUT);
  }

  private Action cleanUp() {
    return new Cmd(requireNonNull(ContextFunctions.DEFAULT_PLACE_HOLDER_FORMATTER))
        .command("/bin/rm")
        .addOption("-rf")
        .append(" ")
        .appendq(baseDir.getAbsolutePath())
        .toAction();
  }

  private Consumer<String> downstreamConsumer() {
    return ((Consumer<String>) System.out::println).andThen(out::add);
  }
}
