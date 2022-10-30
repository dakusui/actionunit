package com.github.dakusui.actionunit.ut.actions.cmd.linux;

import com.github.dakusui.actionunit.actions.cmd.unix.SshOptions;
import com.github.dakusui.actionunit.actions.cmd.unix.Touch;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static com.github.dakusui.crest.Crest.*;

public class TouchTest extends CommanderTestBase {
  @Test
  public void givenExistingFile$whenTouchAndWaitOneSec$thenMtimeUpdated()
      throws IOException, InterruptedException {
    File target = this.createTempFile("hello");
    long lastModified = target.lastModified();
    System.out.println(lastModified);
    Thread.sleep(1000);
    performAsAction(newTouch().noCreate().file(target.getAbsolutePath()));
    assertThat(
        target,
        asLong("lastModified").gt(lastModified).$()
    );
  }

  @Test
  public void givenNonExistingFile$whenTouch$thenComesToExist() {
    File target = new File(baseDir(), "hello");
    requireThat(target, asBoolean("exists").isFalse().$());
    performAsAction(newTouch().file(target.getAbsolutePath()));
    assertThat(
        target,
        asBoolean("exists").isTrue().$()
    );
  }

  private Touch newTouch() {
    return touch();
  }
}
