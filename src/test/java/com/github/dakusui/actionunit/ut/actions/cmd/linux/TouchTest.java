package com.github.dakusui.actionunit.ut.actions.cmd.linux;

import com.github.dakusui.actionunit.actions.cmd.linux.Touch;
import com.github.dakusui.actionunit.core.context.ContextFunctions;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import static com.github.dakusui.crest.Crest.asLong;
import static com.github.dakusui.crest.Crest.assertThat;

public class TouchTest {
  private final List<String> out = new LinkedList<>();

  @Test
  public void test1() throws IOException, InterruptedException {
    File target = CatTest.createTempFile("hello");
    long lastModified = target.lastModified();
    System.out.println(lastModified);
    Thread.sleep(1000);
    performAsAction(newTouch().noCreate().file(target.getAbsolutePath()));
    assertThat(
        target,
        asLong("lastModified").gt(lastModified).$()
    );
  }

  private void performAsAction(Touch touch) {
    ReportingActionPerformer.create().performAndReport(
        touch.downstreamConsumer(downstreamConsumer())
            .toAction(),
        Writer.Std.OUT
    );
  }

  private Consumer<String> downstreamConsumer() {
    return ((Consumer<String>) System.out::println).andThen(out::add);
  }

  private Touch newTouch() {
    return new Touch(ContextFunctions.DEFAULT_PLACE_HOLDER_FORMATTER);
  }
}
