package com.github.dakusui.actionunit.extras.cmd.linux;

import com.github.dakusui.actionunit.extras.cmd.FsTestBase;
import com.github.dakusui.actionunit.core.Action;
import com.github.dakusui.actionunit.linux.Ls;
import com.github.dakusui.actionunit.linux.Touch;
import org.junit.Test;

import java.io.IOException;

import static com.github.dakusui.actionunit.core.ActionSupport.named;
import static com.github.dakusui.actionunit.core.ActionSupport.sequential;
import static com.github.dakusui.crest.Crest.*;
import static java.util.Arrays.asList;

public class LsTest extends FsTestBase<Ls> {

  public LsTest() throws IOException {
  }

  @Override
  protected Action preparation() {
    return named(
        "prepare test file",
        sequential(
            new Touch().cwd(dir).add("a").build(),
            new Touch().cwd(dir).add("b").build(),
            new Touch().cwd(dir).add("c").build()
        ));
  }

  @Test
  public void none() {
    perform(commander.build());
    assertThat(
        stdout,
        allOf(
            asInteger("size").equalTo(3).$(),
            // Let's not care order of elements.
            asListOf(String.class).containsExactly(asList("a", "b", "c")).$()
        ));
  }

  @Test
  public void longListing() {
    perform(commander.longListing().build());
    assertThat(
        stdout,
        allOf(
            asInteger("size").equalTo(4).$(),
            // Let's not care order of elements.
            asString("get", 0).containsString("total").$(),
            asString("get", 1).endsWith("a").$(),
            asString("get", 2).endsWith("b").$(),
            asString("get", 3).endsWith("c").$()
        ));
  }

  @Test
  public void all() {
    perform(commander.all().build());
    assertThat(
        stdout,
        allOf(
            asInteger("size").equalTo(5).$(),
            // Let's not care order of elements.
            asListOf(String.class).containsExactly(asList(".", "..", "a", "b", "c")).$()
        ));
  }

  @Test
  public void humanReadable() {
    perform(commander.humanReadable().longListing().build());
    assertThat(
        stdout,
        allOf(
            asInteger("size").equalTo(4).$(),
            // Let's not care order of elements.
            asString("get", 0).containsString("total").$(),
            asString("get", 1).endsWith("a").$(),
            asString("get", 2).endsWith("b").$(),
            asString("get", 3).endsWith("c").$()
        ));
  }

  @Test
  public void classify() {
    perform(commander.classify().build());
  }

  @Test
  public void reverse() {
    perform(commander.reverse().build());
  }

  @Test
  public void sortByMtime() {
    perform(commander.sortByMtime().build());
  }

  @Test
  public void latr() {
    perform(commander.longListing().all().sortByMtime().reverse().build());
  }

  @Override
  protected Ls create() {
    return new Ls();
  }
}