package com.github.dakusui.actionunit.extras.cmdaction;

import com.github.dakusui.actionunit.core.Context;
import com.github.dakusui.actionunit.extras.cmd.linux.Echo;
import org.junit.Test;

import java.util.stream.StreamSupport;

import static com.github.dakusui.crest.Crest.*;
import static java.util.stream.Collectors.toList;

public class CommanderTest extends CommanderTestBase<Echo> {
  @Override
  protected Echo create(Context context) {
    return new Echo(context);
  }

  @Test
  public void test() {

    // time out (enable/disable)
    // retry (interval)
    // stdin (giving stream)
    // stdout, stderr (transforming output)
    // stdout, stderr (consuming output)
    // cmdaction
    perform(commander
        .noTrailingNewline().enableBackslashInterpretation().addq("hello\\nworld\\n")
        .consumeStdoutWith(System.err::println)
        .build()
    );
  }

  @Test
  public void toCmd() {
    assertThat(
        this.commander
            .noTrailingNewline()
            .enableBackslashInterpretation().addq("hello\\nworld\\n")
            .toCmd()
            .stream()
            .collect(toList()),
        allOf(
            asInteger("size").eq(2).$(),
            asString("get", 0).equalTo("hello").$(),
            asString("get", 1).equalTo("world").$()
        )
    );
  }

  @Test
  public void toIterable() {
    assertThat(
        StreamSupport.stream(
            this.commander
                .noTrailingNewline()
                .enableBackslashInterpretation().addq("hello\\nworld\\n")
                .toIterable()
                .spliterator(),
            false
        ).collect(
            toList()
        ),
        allOf(
            asInteger("size").eq(2).$(),
            asString("get", 0).equalTo("hello").$(),
            asString("get", 1).equalTo("world").$()
        ));
  }

}