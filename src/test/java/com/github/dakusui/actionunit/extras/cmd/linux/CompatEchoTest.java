package com.github.dakusui.actionunit.extras.cmd.linux;

import com.github.dakusui.actionunit.extras.cmd.CommanderTestBase;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.linux.compat.CompatEcho;
import org.junit.Test;

import static com.github.dakusui.actionunit.ut.utils.TestUtils.isRunUnderLinux;
import static com.github.dakusui.crest.Crest.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assume.assumeTrue;

public class CompatEchoTest extends CommanderTestBase<CompatEcho> {
  @Test
  public void noTrailingNewline() {
    perform(this.commander.noTrailingNewline().addq(helloWorld(context)).build());
    assertThat(
        this.stdout,
        allOf(
            asInteger("size").eq(1).$(),
            asListOf(String.class).equalTo(singletonList(helloWorld(context))).$()
        ));
  }

  @Test
  public void enableBackslashInterpretation() {
    assumeTrue(isRunUnderLinux());
    perform(this.commander.enableBackslashInterpretation().message(this::helloWorld).build());
    assertThat(
        this.stdout,
        allOf(
            asInteger("size").eq(3).$(),
            asListOf(String.class).equalTo(asList("hello", "world", "")).$()
        ));
    assertThat(
        this.stderr,
        asListOf(String.class).equalTo(emptyList()).$()
    );
  }

  @Test
  public void noTrailingNewlineAndEnableBackslashInterpretation() {
    assumeTrue(isRunUnderLinux());
    perform(this.commander.noTrailingNewline().enableBackslashInterpretation().addq(helloWorld(context)).build());
    assertThat(
        this.stdout,
        allOf(
            asInteger("size").eq(2).$(),
            asListOf(String.class).equalTo(asList("hello", "world")).$()
        ));
    assertThat(
        this.stderr,
        asListOf(String.class).equalTo(emptyList()).$()
    );
  }

  @Test
  public void disableBackslashInterpretation() {
    assumeTrue(isRunUnderLinux());
    perform(this.commander.disableBackslashInterpretation().addq(helloWorld(context)).build());
    assertThat(
        this.stdout,
        allOf(
            asInteger("size").eq(1).$(),
            asListOf(String.class).equalTo(singletonList(helloWorld(context))).$()
        ));
    assertThat(
        this.stderr,
        asListOf(String.class).equalTo(emptyList()).$()
    );
  }

  @Override
  protected CompatEcho create() {
    return new CompatEcho();
  }

  private String helloWorld(Context context) {
    return "hello\\nworld\\n";
  }
}
