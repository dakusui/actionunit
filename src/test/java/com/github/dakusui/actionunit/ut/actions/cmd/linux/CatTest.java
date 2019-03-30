package com.github.dakusui.actionunit.ut.actions.cmd.linux;

import com.github.dakusui.actionunit.actions.cmd.unix.Cat;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static com.github.dakusui.actionunit.core.context.ContextFunctions.immediateOf;
import static com.github.dakusui.crest.Crest.*;
import static com.github.dakusui.crest.utils.printable.Predicates.matchesRegex;
import static java.util.Arrays.asList;

public class CatTest extends CommanderTestBase {
  @Test
  public void givenCatHelloWorldUsingHereDoc_notQuoted$whenPerformAsAction$thenHelloWorldIsPrinted() {
    performAsAction(
        newCat()
            .beginHereDocument("HELLO")
            .writeln("hello")
            .write("world")
            .newLine()
            .endHereDocument());
    assertThat(
        out(),
        asListOf(String.class).equalTo(asList("hello", "world")).$()
    );
  }

  @Test
  public void givenCatHelloWorldUsingHereDoc_withLineNumber$whenPerformAsAction$thenHelloWorldIsPrinted() {
    performAsAction(
        newCat()
            .lineNumber()
            .beginHereDocument("HELLO")
            .writeln("hello")
            .write("world")
            .newLine()
            .endHereDocument());
    assertThat(
        out(),
        asListOf(
            String.class,
            sublistAfter(matchesRegex(".+1\thello")).after(matchesRegex(".+2\tworld")).$())
            .isEmpty().$());
  }

  @Test
  public void givenFileContainingHelloWorld$whenCatUsingFile$thenPrintedCorrectly() throws IOException {
    File file = this.createTempFile("hello", "world");
    performAsAction(newCat().file(file));
    assertThat(
        out(),
        asListOf(
            String.class,
            sublistAfter(matchesRegex("hello")).after(matchesRegex("world")).$())
            .isEmpty().$());
  }

  @Test
  public void givenFileContainingHelloWorld$whenCatUsingString$thenPrintedCorrectly() throws IOException {
    File file = this.createTempFile("hello", "world");
    performAsAction(newCat().file(file.getAbsolutePath()));
    assertThat(
        out(),
        asListOf(
            String.class,
            sublistAfter(matchesRegex("hello")).after(matchesRegex("world")).$())
            .isEmpty().$());
  }

  @Test
  public void givenFileContainingHelloWorldUsingContextFunction$whenCatUsingString$thenPrintedCorrectly() throws IOException {
    File file = this.createTempFile("hello", "world");
    performAsAction(newCat().file(immediateOf(file.getAbsolutePath())));
    assertThat(
        out(),
        asListOf(
            String.class,
            sublistAfter(matchesRegex("hello")).after(matchesRegex("world")).$())
            .isEmpty().$());
  }

  private Cat newCat() {
    return cat();
  }
}
