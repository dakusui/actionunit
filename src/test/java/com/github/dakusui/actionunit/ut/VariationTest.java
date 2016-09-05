package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.Actions;
import com.github.dakusui.actionunit.Context;
import com.github.dakusui.actionunit.connectors.Connectors;
import com.github.dakusui.actionunit.connectors.Pipe;
import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import com.google.common.base.Function;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static com.github.dakusui.actionunit.Actions.forEach;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;

public class VariationTest {
  @Test
  public void doubleLoop() {
    final List<String> list = new LinkedList<>();
    forEach(asList("a", "b"),
        forEach(asList("1", "2"),
            new Sink.Base<String>() {
              @Override
              public void apply(String input, Object... outer) {
                list.add(String.format("%s-%s", outer[0], input));
              }
            }
        )
    ).accept(new ActionRunner.Impl());
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
        asList("a", "b"),
        new Sink.Base<String>() {
          @Override
          public void apply(String input, Object... outer) {
          }
        }
    ).accept(new ActionRunner.Impl());
  }


  @Test
  public void testAction1() {
    Actions.forEach(
        asList("host1", "host2"),
        Actions.<String, Integer>test()
            .when(new Function<String, Integer>() {
              @Override
              public Integer apply(String input) {
                return Integer.parseInt(input.substring(input.length() - 1));
              }
            })
            .then(notNullValue())
            .build()
    ).accept(new ActionRunner.Impl());
  }

  @Test
  public void testAction2() {
    forEach(
        asList("host1", "host2"),
        Actions.<String, Integer>test()
            .given("9")
            .when(new Pipe<String, Integer>() {
              @Override
              public Integer apply(String input, Context context) {
                return Integer.parseInt(input.substring(input.length() - 1));
              }
            })
            .then(Connectors.<Integer>dumb())
            .build()
    ).accept(new ActionRunner.Impl());
  }

  @Test
  public void testAction3() {
    forEach(
        asList("host1", "host2"),
        Actions.<String, Integer>test()
            .given(Connectors.<String>context())
            .when(new Pipe<String, Integer>() {
              @Override
              public Integer apply(String input, Context context) {
                return Integer.parseInt(input.substring(input.length() - 1));
              }
            })
            .then(Connectors.<Integer>dumb())
            .build()
    ).accept(new ActionRunner.Impl());
  }
}
