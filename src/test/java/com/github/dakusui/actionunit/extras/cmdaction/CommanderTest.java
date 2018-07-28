package com.github.dakusui.actionunit.extras.cmdaction;

import com.github.dakusui.actionunit.n.extras.linux.Echo;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.github.dakusui.actionunit.compat.utils.TestUtils.isRunUnderLinux;
import static com.github.dakusui.crest.Crest.*;
import static java.util.stream.Collectors.toList;
import static org.junit.Assume.assumeTrue;

public class CommanderTest extends CommanderTestBase<Echo> {
  @Override
  protected Echo create() {
    return new Echo();
  }

  @Test
  public void test() {

    // time out (enable/disable)
    // retry (interval)
    // stdin (giving stream)
    // stdout, stderr (transforming output)
    // stdout, stderr (consuming output)
    // cmdaction
    commander.cmdBuilder().consumeStdout(System.err::println);
    perform(commander
        .noTrailingNewline().enableBackslashInterpretation().addq("hello\\nworld\\n")
        .build()
    );
  }

  @Test
  public void cwdDefault() {
    perform(
        commander
            .add("$(pwd)")
            .build()
    );
    assertThat(
        this.stdout,
        asString("get", 0).equalTo(this.commander.cwd().getAbsolutePath()).$()
    );
  }

  @Test
  public void cwdExplicit() throws IOException {
    assumeTrue(isRunUnderLinux());
    File tmp = Files.createTempDirectory("tmp").toFile();
    tmp.deleteOnExit();
    perform(
        commander
            .cwd(tmp)
            .add("$(pwd)")
            .build()
    );
    assertThat(
        this.stdout,
        asString("get", 0).equalTo(tmp.getAbsolutePath()).$()
    );
  }

  @Test
  public void toCmd() {
    assumeTrue(isRunUnderLinux());
    assertThat(
        this.commander
            .noTrailingNewline()
            .enableBackslashInterpretation().addq("hello\\nworld\\n")
            .toCmd(context)
            .stream()
            .collect(toList()),
        allOf(
            asInteger("size").eq(2).$(),
            asString("get", 0).equalTo("hello").$(),
            asString("get", 1).equalTo("world").$()
        )
    );
  }

  @Test
  public void describe() {
    assertThat(
        this.commander
            .stdin(Stream.empty()) // to cover stdin() method
            .disconnectStdin() // to cover disconnectStdin() method
            .disableTimeout() // to cover disableTimeout() method()
            .describe("describing echo hello")
            .addq(context -> "hello")
            .build(),
        asString("toString").equalTo("describing echo hello").$()
    );
  }

  @Test
  public void env() {
    perform(
        this.commander
            .env("HELLO", "WORLD")
            .add("${HELLO}")
            .build()
    );
    assertThat(
        this.stdout,
        allOf(
            asInteger("size").equalTo(1).$(),
            asString("get", 0).equalTo("WORLD").$()
        ));
  }

  @Test
  public void toIterable() {
    assumeTrue(isRunUnderLinux());
    assertThat(
        StreamSupport.stream(
            this.commander
                .noTrailingNewline()
                .enableBackslashInterpretation().addq("hello\\nworld\\n")
                .toIterable(context)
                .spliterator(),
            false
        ).collect(
            toList()
        ),
        allOf(
            asInteger("size").eq(2).$(),
            asString("get", 0).equalTo("hello").$(),
            asString("get", 1).equalTo("world").$()
        ));
  }

  @Test
  public void toStream() {
    assumeTrue(isRunUnderLinux());
    assertThat(
        this.commander
            .noTrailingNewline()
            .enableBackslashInterpretation().addq("hello\\nworld\\n")
            .toStream(context)
            .collect(toList()),
        allOf(
            asInteger("size").eq(2).$(),
            asString("get", 0).equalTo("hello").$(),
            asString("get", 1).equalTo("world").$()
        ));
  }

  @Test
  public void consumeStdoutWith() {
    perform(
        this.commander
            .noTrailingNewline()
            .enableBackslashInterpretation()
            .message("hello'world")
            .build()
    );
  }
}