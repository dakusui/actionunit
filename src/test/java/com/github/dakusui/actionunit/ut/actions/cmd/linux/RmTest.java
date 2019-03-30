package com.github.dakusui.actionunit.ut.actions.cmd.linux;

import com.github.dakusui.actionunit.actions.cmd.unix.Rm;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static com.github.dakusui.crest.Crest.*;

public class RmTest extends CommanderTestBase {
  @Test
  public void givenExistingFile$whenRm$thenRemoved() throws IOException {
    File target = createNewFile("hello");
    requireThat(target, asBoolean("exists").isTrue().$());
    performAsAction(newRm().file(target));
    assertThat(
        target,
        asBoolean("exists").isFalse().$()
    );
  }

  @Test
  public void givenExistingDirectory$whenRmRF$thenRemoved() {
    File target = createNewDir("hello");
    requireThat(
        target,
        allOf(
            asBoolean("exists").isTrue().$(),
            asBoolean("isDirectory").isTrue().$()));
    performAsAction(newRm().force().recursive().file(target));
    assertThat(
        target,
        asBoolean("exists").isFalse().$()
    );
  }

  private Rm newRm() {
    return rm();
  }
}
