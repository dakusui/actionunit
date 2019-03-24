package com.github.dakusui.actionunit.ut.actions.cmd.linux;

import com.github.dakusui.actionunit.actions.cmd.linux.Cat;
import com.github.dakusui.actionunit.core.context.ContextFunctions;
import com.github.dakusui.actionunit.io.Writer;
import com.github.dakusui.actionunit.visitors.ReportingActionPerformer;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import static com.github.dakusui.crest.Crest.asListOf;
import static com.github.dakusui.crest.Crest.assertThat;
import static com.github.dakusui.crest.Crest.sublistAfter;
import static com.github.dakusui.crest.utils.printable.Predicates.matchesRegex;
import static java.lang.String.format;
import static java.util.Arrays.asList;

public class CatTest {
  private final List<String> out = new LinkedList<>();

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
        out,
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
        out,
        asListOf(
            String.class,
            sublistAfter(matchesRegex(".+1\thello")).after(matchesRegex(".+2\tworld")).$())
            .isEmpty().$());
  }

  @Test
  public void test() throws IOException {
    File file = createTempFile("hello", "world");
    performAsAction(newCat().file(file));
    assertThat(
        out,
        asListOf(
            String.class,
            sublistAfter(matchesRegex("hello")).after(matchesRegex("world")).$())
            .isEmpty().$());
  }

  @Test
  public void test2() throws IOException {
    File file = createTempFile("hello", "world");
    performAsAction(newCat().file(file.getAbsolutePath()));
    assertThat(
        out,
        asListOf(
            String.class,
            sublistAfter(matchesRegex("hello")).after(matchesRegex("world")).$())
            .isEmpty().$());
  }

  static File createTempFile(String... texts) throws IOException {
    File file = File.createTempFile("cmd-test", "tmp");
    file.deleteOnExit();
    try (FileWriter writer = new FileWriter(file)) {
      for (String each : texts) {
        writer.write(each);
        writer.write(format("%n"));
      }
    }
    return file;
  }

  private void performAsAction(Cat cat) {
    ReportingActionPerformer.create().performAndReport(
        cat.downstreamConsumer(downstreamConsumer())
            .toAction(),
        Writer.Std.OUT
    );
  }

  private Consumer<String> downstreamConsumer() {
    return ((Consumer<String>) System.out::println).andThen(out::add);
  }

  private Cat newCat() {
    return new Cat(ContextFunctions.DEFAULT_PLACE_HOLDER_FORMATTER);
  }
}
