package com.github.dakusui.actionunit.ut;

import com.github.dakusui.actionunit.CompatActions;
import com.github.dakusui.actionunit.Context;
import com.github.dakusui.actionunit.connectors.Connectors;
import com.github.dakusui.actionunit.connectors.Pipe;
import com.github.dakusui.actionunit.connectors.Sink;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static com.github.dakusui.actionunit.CompatActions.foreach;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;

public class VariationTest {
  @Test
  public void doubleLoop() {
    final List<String> list = new LinkedList<>();
    CompatActions.foreach(asList("a", "b"),
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
    CompatActions.foreach(
        asList("host1", "host2"),
        CompatActions.<String, Object>test()
            .when(input -> Integer.parseInt(input.substring(input.length() - 1)))
            .then(notNullValue())
            .build()
    ).accept(new ActionRunner.Impl());
  }

  @Test
  public void testAction2() {
    CompatActions.foreach(
        asList("host1", "host2"),
        CompatActions.<String, Integer>test()
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
    CompatActions.foreach(
        asList("host1", "host2"),
        CompatActions.<String, Integer>test()
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
