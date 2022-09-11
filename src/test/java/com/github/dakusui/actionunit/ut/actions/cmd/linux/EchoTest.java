package com.github.dakusui.actionunit.ut.actions.cmd.linux;

import com.github.dakusui.actionunit.actions.cmd.unix.Echo;
import com.github.dakusui.actionunit.core.context.StreamGenerator;
import com.github.dakusui.actionunit.ut.utils.TestUtils;
import org.junit.Test;

import static com.github.dakusui.actionunit.core.ActionSupport.forEach;
import static com.github.dakusui.actionunit.core.context.ContextFunctions.contextValueOf;
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
        forEach("i", StreamGenerator.fromArray("hello", "world")).perform(
            initCommander(newEcho())
                .noTrailingNewLine()
                .message(contextValueOf("i")).toAction()
        )
    );
    assertThat(
        out(),
        asListOf(String.class, sublistAfterElement("hello").afterElement("world").$()).isEmpty().$()
    );
  }

  @Test
  public void givenEchoHelloWorldWithContextFunctionQuoted$whenPerformAsAction$thenOnlyHelloAndWorldAreWritten() {
    performAction(
        forEach("i", StreamGenerator.fromArray("hello", "'world'")).perform(
            initCommander(newEcho())
                .noTrailingNewLine()
                .message(contextValueOf("i")).toAction()
        )
    );
    assertThat(
        out(),
        asListOf(String.class, sublistAfterElement("hello").afterElement("'world'").$()).isEmpty().$()
    );
  }

  private Echo newEcho() {
    return echo();
  }
}
