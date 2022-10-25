package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.ut.utils.TestUtils;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static com.github.dakusui.actionunit.core.ActionSupport.*;
import static com.github.dakusui.actionunit.ut.utils.TestUtils.createActionPerformer;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class VariationTest extends TestUtils.TestBase {
  @Test
  public void doubleLoop() {
    final List<String> list = new LinkedList<>();
    forEach("i", (c) -> Stream.of("a", "b")).perform(b ->
        forEach("j", (c) -> Stream.of("1", "2")).perform(bb ->
            simple(
                "add string",
                (context) -> list.add(String.format("%s-%s",
                    b.resolveValue(context),
                    bb.resolveValue(context)
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
        (c) -> Stream.of("a", "b")
    ).perform(
        nop()
    ).accept(createActionPerformer());
  }
}
