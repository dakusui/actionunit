package com.github.dakusui.actionunit.ut.actions.cmd.linux;

import com.github.dakusui.actionunit.actions.ForEach2;
import com.github.dakusui.actionunit.actions.cmd.unix.Echo;
import com.github.dakusui.actionunit.core.ActionSupport;
import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.core.context.StreamGenerator;
import com.github.dakusui.actionunit.ut.utils.TestUtils;
import com.github.dakusui.printables.PrintableFunctionals;
import org.junit.Test;

import java.util.function.Function;

import static com.github.dakusui.actionunit.ut.utils.TestUtils.assumeRunningOnLinux;
import static com.github.dakusui.crest.Crest.*;
import static java.util.Collections.singletonList;

public class EchoTest extends CommanderTestBase {
  @Test
  public void givenEchoHelloWorld$whenPerformAsAction$thenHelloWorldIsPrinted() {
    performAsAction(newEcho().message("hello world"));
    assertThat(
        out(),
        asListOf(String.class).equalTo(singletonList("hello world")).$()
    );
  }

  @Test
  public void givenSingleQuoteContainingMessage$whenPerformAsAction$thenCorrectMessageIsPrinted() {
    String message = "hello, world's best message";
    performAsAction(newEcho().message(message));
    assertThat(
        out(),
        asListOf(String.class).equalTo(singletonList(message)).$()
    );
  }

  @Test
  public void givenSingleQuoteAndNewLineContainingMessage$whenPerformAsAction$thenCorrectMessageIsPrinted() {
    assumeRunningOnLinux();
    performAsAction(newEcho()
        .disableBackslashInterpretation()
        .message("hello, world's\nbest message"));
    assertThat(
        TestUtils.removeSpentTimeFromResultColumn(out()),
        asListOf(
            String.class,
            sublistAfterElement("hello, world's").afterElement("best message").$())
            .isEmpty().$()
    );
  }

  @Test
  public void givenSingleQuoteAndEscapedNewLineContainingMessage_enablingBackslaceInterpretation$whenPerformAsAction$thenCorrectMessageIsPrinted() {
    assumeRunningOnLinux();
    performAsAction(newEcho()
        .enableBackslashInterpretation()
        .message("hello, world's\\nbest message"));
    assertThat(
        TestUtils.removeSpentTimeFromResultColumn(out()),
        asListOf(
            String.class,
            sublistAfterElement("hello, world's").afterElement("best message").$())
            .isEmpty().$()
    );
  }

  @Test
  public void givenHello_EscapedNewLine_World_disblingBackslaceInterpretation$whenPerformAsAction$thenCorrectMessageIsPrinted() {
    assumeRunningOnLinux();
    performAsAction(newEcho()
        .disableBackslashInterpretation()
        .message("hello\\nworld"));
    assertThat(
        TestUtils.removeSpentTimeFromResultColumn(out()),
        asListOf(String.class).equalTo(singletonList("hello\\nworld")).$()
    );
  }

  @Test
  public void givenEchoHelloWorld$whenPerformAsAction$thenOnlyHello_World_AndNewLineAreWritten() {
    performAsAction(newEcho().message("hello\nworld\n"));
    assertThat(
        out(),
        asListOf(String.class, sublistAfterElement("hello").afterElement("world").afterElement("").$()).isEmpty().$()
    );
  }

  @Test
  public void givenEchoHelloWorldAndNewLineWithNoTrailingNewLine$whenPerformAsAction$thenOnlyHelloAndWorldAreWritten() {
    performAsAction(newEcho()
        .noTrailingNewLine()
        .message("hello\nworld\n"));
    assertThat(
        out(),
        asListOf(String.class, sublistAfterElement("hello").afterElement("world").$()).isEmpty().$()
    );
  }

  @Test
  public void givenEchoHelloWorldWithContextFunction$whenPerformAsAction$thenOnlyHelloAndWorldAreWritten() {
    performAction(
        ActionSupport.forEach("i", StreamGenerator.fromArray("hello", "world")).perform(
            b -> initCommander(newEcho())
                .noTrailingNewLine()
                .message(contextVariable(b)).toAction()
        )
    );
    assertThat(
        out(),
        asListOf(String.class, sublistAfterElement("hello").afterElement("world").$()).isEmpty().$()
    );
  }

  private static Function<Context, String> contextVariable(ForEach2.Builder<String> b) {
    return PrintableFunctionals.printableFunction(b::contextVariable).describe("con");
  }

  @Test
  public void givenEchoHelloWorldWithContextFunctionQuoted$whenPerformAsAction$thenOnlyHelloAndWorldAreWritten() {
    performAction(
        ActionSupport.forEach("i", StreamGenerator.fromArray("hello", "'world'")).perform(
            b -> initCommander(newEcho())
                .noTrailingNewLine()
                .message(contextVariable(b)).toAction()
        )
    );
    assertThat(
        out(),
        asListOf(String.class, sublistAfterElement("hello").afterElement("'world'").$()).isEmpty().$()
    );
  }

  @Test
  public void test() {
    System.getProperties().keySet().stream().sorted().forEach((k) -> System.out.printf("%-20s=%-20s%n", k, System.getProperties().get(k)));
  }

  private Echo newEcho() {
    return echo();
  }
}
