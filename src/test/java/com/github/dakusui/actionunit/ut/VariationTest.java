package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.compat.utils.TestUtils;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static com.github.dakusui.actionunit.compat.utils.TestUtils.createActionPerformer;
import static com.github.dakusui.actionunit.n.core.ActionSupport.*;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class VariationTest extends TestUtils.TestBase {
  @Test
  public void doubleLoop() {
    final List<String> list = new LinkedList<>();
    forEach("i", () -> Stream.of("a", "b")).perform(
        forEach("j", () -> Stream.of("1", "2")).perform(
            simple(
                "add string",
                (context) -> list.add(String.format("%s-%s",
                    context.valueOf("i"),
                    context.valueOf("j")
                ))))
    ).accept(createActionPerformer());
    assertEquals(
        asList(
            "a-1",
            "a-2",
            "b-1",
            "b-2"
        ),
        list
    );
  }

  @Test
  public void forEachAndPipedAction() {
    forEach(
        "i",
        () -> Stream.of("a", "b")
    ).perform(
        nop()
    ).accept(createActionPerformer());
  }
}
