package com.github.dakusui.actionunit.extras.cmd.linux;

import com.github.dakusui.actionunit.extras.cmd.FsTestBase;
import com.github.dakusui.actionunit.linux.CompatCat;
import org.junit.Test;

import java.io.IOException;
import java.util.stream.Stream;

import static com.github.dakusui.crest.Crest.*;

public class CompatCatTest extends FsTestBase<CompatCat> {
  public CompatCatTest() throws IOException {
  }

  @Test
  public void number() {
    perform(this.commander.stdin((c) -> Stream.of("a", "b", "c")).number().build());
    assertThat(
        this.stdout,
        allOf(
            asInteger("size").equalTo(3).$(),
            asString("get", 0).containsString("1").$(),
            asString("get", 0).containsString("a").$(),
            asString("get", 1).containsString("2").$(),
            asString("get", 1).containsString("b").$(),
            asString("get", 2).containsString("3").$(),
            asString("get", 2).containsString("c").$()
        )
    );
  }

  @Override
  protected CompatCat create() {
    return new CompatCat(context);
  }
}
