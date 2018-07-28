package com.github.dakusui.actionunit.extras.cmdaction.linux;

import com.github.dakusui.actionunit.extras.cmdaction.CommanderTestBase;
import com.github.dakusui.actionunit.n.core.Context;
import com.github.dakusui.actionunit.n.extras.linux.Echo;
import org.junit.Test;

import static com.github.dakusui.actionunit.compat.utils.TestUtils.isRunUnderLinux;
import static com.github.dakusui.crest.Crest.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assume.assumeTrue;

public class EchoTest extends CommanderTestBase<Echo> {
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
  protected Echo create() {
    return new Echo();
  }

  private String helloWorld(Context context) {
    return "hello\\nworld\\n";
  }
}
