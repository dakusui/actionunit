package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.core.Context;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static com.github.dakusui.actionunit.utils.TestUtils.createActionPerformer;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class VariationTest implements Context {
  @Test
  public void doubleLoop() {
    final List<String> list = new LinkedList<>();
    forEachOf(asList("a", "b")).perform(
        ($, i) -> forEachOf(asList("1", "2")).perform(
            ($$, j) -> simple(
                "add string",
                () -> list.add(String.format("%s-%s", i.get(), j.get()))
            )
        )
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
    forEachOf(
        asList("a", "b")
    ).perform(
        ($, i) -> nop()
    ).accept(createActionPerformer());
  }

  @Test
  public void testAction1() {
    forEachOf(
        asList("host1", "host2")
    ).perform(
        ($, i) -> this.<String, Integer>given("given data", i)
            .when("when parse int", value -> Integer.parseInt(value.substring(value.length() - 1)))
            .then("then non-null returned", Objects::nonNull)
    ).accept(createActionPerformer());
  }

  @Test
  public void testAction2() {
    forEachOf(asList("host1", "host2")).perform(
        ($, i) -> this.<String, Integer>given("'9' is given", () -> "9")
            .when("when parseInt", value -> Integer.parseInt(value.substring(value.length() - 1)))
            .then("then passes (always)", out -> true)
    ).accept(createActionPerformer());
  }

  @Test
  public void testAction3() {
    forEachOf(asList("host1", "host2")).perform(
        ($, hostName) ->
            this.given("Host name is given", hostName)
                .when("", input -> Integer.parseInt(input.substring(input.length() - 1)))
                .then("then passes (always)", out -> true)
    ).accept(createActionPerformer());
  }
}
