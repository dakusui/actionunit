package com.github.dakusui.actionunit.tests.ut;

import com.github.dakusui.actionunit.Actions;
import com.github.dakusui.actionunit.Context;
import com.github.dakusui.actionunit.connectors.Connectors;
import com.github.dakusui.actionunit.connectors.Pipe;
import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import com.google.common.base.Function;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static com.github.dakusui.actionunit.Actions.foreach;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class VariationTest {
  @Test
  public void doubleLoop() {
    final List<String> list = new LinkedList<>();
    Actions.foreach(asList("a", "b"),
        foreach(asList("1", "2"),
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
    foreach(
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
    Actions.foreach(
        asList("host1", "host2"),
        Actions.<String, Integer>test()
            .when(new Function<String, Integer>() {
              @Override
              public Integer apply(String input) {
                return Integer.parseInt(input.substring(input.length() - 1));
              }
            })
            .then(CoreMatchers.<Integer>notNullValue())
            .build()
    ).accept(new ActionRunner.Impl());
  }

  @Test
  public void testAction2() {
    Actions.foreach(
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
    Actions.foreach(
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
