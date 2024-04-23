package com.github.dakusui.actionunit.ut.actions.cmd.linux;

import com.github.dakusui.actionunit.actions.cmd.Commander;
import com.github.dakusui.actionunit.actions.cmd.CommanderConfig;
import com.github.dakusui.actionunit.actions.cmd.UnixCommanderFactory;
import com.github.dakusui.actionunit.actions.cmd.unix.Cmd;
import com.github.dakusui.actionunit.actions.cmd.unix.SshOptions;
import com.github.dakusui.actionunit.core.Action;
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
import java.util.stream.Stream;

import static com.github.dakusui.crest.Crest.asBoolean;
import static com.github.dakusui.crest.Crest.requireThat;
import static java.lang.String.format;

public abstract class CommanderTestBase extends TestUtils.TestBase implements UnixCommanderFactory {
  private             File                 baseDir;
  private final       List<String>         out      = new LinkedList<>();

  @Before
  public void setUp() throws IOException {
    this.baseDir = Files.createTempDirectory("cmd-test-").toFile();
  }

  @After
  public void tearDown() {
    performAction(cleanUp());
  }

  @Override
  public CommanderConfig config() {
    return CommanderConfig.DEFAULT;
  }

  List<String> out() {
    return Collections.unmodifiableList(out);
  }

  void performAsAction(Commander<?> commander) {
    this.performAction(initCommander(commander).toAction());
  }

  Stream<String> performWithRunMethod(Commander<?> commander) {
    return initCommander(commander).run();
  }

  @SuppressWarnings("unchecked")
  <C extends Commander<?>> C initCommander(C commander) {
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
    File file = fileOf(name);
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
    File ret = fileOf(name);
    requireThat(ret.mkdirs(), asBoolean().isTrue().$());
    return ret;
  }

  File baseDir() {
    return this.baseDir;
  }

  File fileOf(String name) {
    return new File(this.baseDir, name);
  }

  String absolutePathOf(String name) {
    return fileOf(name).getAbsolutePath();
  }

  void performAction(Action action) {
    ReportingActionPerformer.create().performAndReport(action, Writer.Std.OUT);
  }

  private Action cleanUp() {
    return new Cmd(CommanderConfig.DEFAULT)
        .commandName("/bin/rm")
        .addOption("-rf")
        .append(" ")
        .appendq(baseDir.getAbsolutePath())
        .toAction();
  }

  private Consumer<String> downstreamConsumer() {
    return ((Consumer<String>) System.out::println).andThen(out::add);
  }
}
