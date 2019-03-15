package com.github.dakusui.actionunit.extras.cmd;

import com.github.dakusui.actionunit.actions.cmd.CommandLineComposer;
import com.github.dakusui.actionunit.actions.cmd.CommanderImpl;
import com.github.dakusui.processstreamer.core.process.Shell;
import org.junit.Test;

import java.util.stream.Stream;

import static com.github.dakusui.actionunit.core.ActionSupport.forEach;
import static com.github.dakusui.actionunit.extras.cmd.CommanderTestUtil.performAndReport;

public class CommanderTest {
  @Test
  public void test() {
    performAndReport(
        forEach("i", c -> Stream.of("hello", "world")).perform(
            commander().command(CommandLineComposer.byIndex("echo {{0}}"), "i").toAction()
        )
    );
  }

  @Test
  public void testNamed() {
    performAndReport(
        forEach("i", c -> Stream.of("hello", "world")).perform(
            commander().command(
                CommandLineComposer.byVariableName(
                    "echo {{i}}", "i"),
                "i").toAction()
        )
    );
  }

  @Test
  public void testImplicitlyNamed() {
    performAndReport(
        forEach("i", c -> Stream.of("hello", "world")).perform(
            commander().command(
                CommandLineComposer.byVariableName(
                    "echo {{i}}"),
                "i").toAction()
        )
    );
  }

  private static CommanderImpl commander() {
    return new CommanderImpl(Shell.LOCAL_SHELL, null);
  }
}
