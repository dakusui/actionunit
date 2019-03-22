package com.github.dakusui.actionunit.ut.actions.cmd.linux;

import com.github.dakusui.actionunit.actions.cmd.linux.Echo;
import com.github.dakusui.actionunit.core.context.ContextFunctions;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import static com.github.dakusui.crest.Crest.*;
import static java.util.Collections.singletonList;

public class EchoTest {
  private final List<String> out = new LinkedList<>();

  @Test
  public void givenEchoHelloWorld_notQuoted$whenPerformAsAction$thenHelloWorldIsPrinted() {
    performEchoAsAction(newEcho().message("hello world"));
    assertThat(
        out,
        asListOf(String.class).equalTo(singletonList("hello world")).$()
    );
  }

  @Test
  public void givenEchoHelloWorld_quoted$whenPerformAsAction$thenHelloWorldIsPrinted() {
    performEchoAsAction(newEcho().message("hello world", true));
    assertThat(
        out,
        asListOf(String.class).equalTo(singletonList("hello world")).$()
    );
  }

  @Test
  public void givenSingleQuoteContainingMessage_quoted$whenPerformAsAction$thenCorrectMessageIsPrinted() {
    String message = "hello, world's best message";
    performEchoAsAction(newEcho().message(message, true));
    assertThat(
        out,
        asListOf(String.class).equalTo(singletonList(message)).$()
    );
  }

  @Test
  public void givenSingleQuoteAndNewLineContainingMessage_quoted$whenPerformAsAction$thenCorrectMessageIsPrinted() {
    performEchoAsAction(newEcho()
        .disableBackslashInterpretation()
        .message("hello, world's\nbest message", true));
    assertThat(
        out,
        asListOf(
            String.class,
            sublistAfterElement("hello, world's").afterElement("best message").$())
            .isEmpty().$()
    );
  }

  @Test
  public void givenSingleQuoteAndEscapedNewLineContainingMessage_quoted_enablingBackslaceInterpretation$whenPerformAsAction$thenCorrectMessageIsPrinted() {
    performEchoAsAction(newEcho()
        .enableBackslashInterpretation()
        .message("hello, world's\\nbest message", true));
    assertThat(
        out,
        asListOf(
            String.class,
            sublistAfterElement("hello, world's").afterElement("best message").$())
            .isEmpty().$()
    );
  }

  @Test
  public void givenHello_EscapedNewLine_World_quoted_disblingBackslaceInterpretation$whenPerformAsAction$thenCorrectMessageIsPrinted() {
    performEchoAsAction(newEcho()
        .disableBackslashInterpretation()
        .message("hello\\nworld", true));
    assertThat(
        out,
        asListOf(String.class).equalTo(singletonList("hello\\nworld")).$()
    );
  }

  @Test
  public void givenEchoHelloWorld$whenPerformAsAction$thenOnlyHello_World_AndNewLineAreWritten() {
    performEchoAsAction(newEcho().message("hello\nworld\n", true));
    assertThat(
        out,
        asListOf(String.class, sublistAfterElement("hello").afterElement("world").afterElement("").$()).isEmpty().$()
    );
  }

  @Test
  public void givenEchoHelloWorldAndNewLineWithNoTrailingNewLine$whenPerformAsAction$thenOnlyHelloAndWorldAreWritten() {
    performEchoAsAction(newEcho()
        .noTrailingNewLine()
        .message("hello\nworld\n", true));
    assertThat(
        out,
        asListOf(String.class, sublistAfterElement("hello").afterElement("world").$()).isEmpty().$()
    );
  }

  private void performEchoAsAction(Echo echo) {
    ReportingActionPerformer.create().performAndReport(
        echo.downstreamConsumer(downstreamConsumer())
            .toAction(),
        Writer.Std.OUT
    );
  }

  private Consumer<String> downstreamConsumer() {
    return ((Consumer<String>) System.out::println).andThen(out::add);
  }

  private Echo newEcho() {
    return new Echo(ContextFunctions.DEFAULT_PLACE_HOLDER_FORMATTER);
  }
}
