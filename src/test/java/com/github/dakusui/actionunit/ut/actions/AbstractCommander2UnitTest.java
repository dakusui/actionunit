package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.actions.cmd.CommanderImpl;
import com.github.dakusui.actionunit.core.context.StreamGenerator;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import com.github.dakusui.processstreamer.core.process.Shell;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import static com.github.dakusui.actionunit.core.ActionSupport.forEach;
import static com.github.dakusui.crest.Crest.asString;
import static com.github.dakusui.crest.Crest.assertThat;

public class AbstractCommander2UnitTest {
  private List<String> out = new LinkedList<>();

  @Test
  public void test() {
    ReportingActionPerformer.create().performAndReport(
        forEach("i", StreamGenerator.fromArray("Hello", "World"))
            .perform(
                new CommanderImpl(Shell.local(), null)
                    .command("echo '{{0}}'", "i")
                    .stdoutConsumer(new Consumer<String>() {
                      @Override
                      public void accept(String s) {
                        out.add(s);
                      }
                    })
                    .toAction()
            ),
        Writer.Std.OUT);
    assertThat(
        out,
        asString("get", 0).$()
    );
  }

  @Test
  public void test2() {
    ReportingActionPerformer.create().performAndReport(
        forEach("i", StreamGenerator.fromArray("Hello", "World"))
            .perform(
                new CommanderImpl(Shell.local(), null)
                    .command("echo '{{0}}'", "i")
                    .stdoutConsumer(new Consumer<String>() {
                      @Override
                      public void accept(String s) {
                        out.add(s);
                      }
                    })
                    .toAction()
            ),
        Writer.Std.OUT);
    assertThat(
        out,
        asString("get", 0).$()
    );
  }
}
