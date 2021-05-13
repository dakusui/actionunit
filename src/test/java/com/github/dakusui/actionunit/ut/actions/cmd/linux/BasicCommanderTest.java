package com.github.dakusui.actionunit.ut.actions.cmd.linux;

import com.github.dakusui.actionunit.actions.Named;
import org.junit.Test;

import java.util.List;

import static com.github.dakusui.crest.Crest.*;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public class BasicCommanderTest extends CommanderTestBase {
  @Test
  public void givenCatHelloWorldUsingHereDoc_notQuoted_withRunMethod$whenPerformAsAction$thenHelloWorldIsPrinted() {
    List<String> out = performWithRunMethod(
        cat().beginHereDocument("HELLO")
            .writeln("hello")
            .write("world")
            .newLine()
            .endHereDocument())
        .collect(toList());
    assertThat(
        out,
        asListOf(String.class).equalTo(asList("hello", "world")).$()
    );
  }

  @Test
  public void testDescribeMethod() {
    assertThat(
        cat().describe("A cat command").toAction(),
        allOf(
            asObject().isInstanceOf(Named.class).$(),
            asObject(call("name").$()).equalTo("A cat command").$()));
  }
}
