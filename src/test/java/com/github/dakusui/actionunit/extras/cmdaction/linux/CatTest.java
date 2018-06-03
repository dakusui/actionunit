package com.github.dakusui.actionunit.extras.cmdaction.linux;

import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.extras.cmd.linux.Cat;
import com.github.dakusui.actionunit.extras.cmdaction.FsTestBase;
import org.junit.Test;

import java.io.IOException;
import java.util.stream.Stream;

import static com.github.dakusui.crest.Crest.*;

public class CatTest extends FsTestBase<Cat> {
  public CatTest() throws IOException {
  }

  @Test
  public void number() {
    perform(this.commander.stdin(Stream.of("a", "b", "c")).number().build());
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
  protected Cat create(Context context) {
    return new Cat(context);
  }
}
