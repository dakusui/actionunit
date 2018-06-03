package com.github.dakusui.actionunit.extras.cmdaction.linux;

import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.extras.cmd.linux.Echo;
import com.github.dakusui.actionunit.extras.cmdaction.CommanderTestBase;
import org.junit.Test;

import static com.github.dakusui.crest.Crest.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class EchoTest extends CommanderTestBase<Echo> {
  @Test
  public void noTrailingNewline() {
    perform(this.commander.noTrailingNewline().addq(helloWorld()).build());
    assertThat(
        this.stdout,
        allOf(
            asInteger("size").eq(1).$(),
            asListOf(String.class).equalTo(singletonList(helloWorld())).$()
        ));
  }

  @Test
  public void enableBackslashInterpretation() {
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
    perform(this.commander.noTrailingNewline().enableBackslashInterpretation().addq(helloWorld()).build());
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
    perform(this.commander.disableBackslashInterpretation().addq(helloWorld()).build());
    assertThat(
        this.stdout,
        allOf(
            asInteger("size").eq(1).$(),
            asListOf(String.class).equalTo(singletonList(helloWorld())).$()
        ));
    assertThat(
        this.stderr,
        asListOf(String.class).equalTo(emptyList()).$()
    );
  }

  @Override
  protected Echo create(Context context) {
    return new Echo(context);
  }

  private String helloWorld() {
    return "hello\\nworld\\n";
  }
}
