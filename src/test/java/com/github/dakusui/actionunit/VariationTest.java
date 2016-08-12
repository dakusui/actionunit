package com.github.dakusui.actionunit;

import com.github.dakusui.actionunit.TestAction.Output.Text;
import com.github.dakusui.actionunit.visitors.ActionRunner;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static com.github.dakusui.actionunit.Actions.forEach;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class VariationTest {
  @Test
  public void doubleLoop() {
    final List<String> list = new LinkedList<>();
    forEach(asList("a", "b"),
        forEach(asList("1", "2"),
            new Block.Base<String>() {
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

  @Test(expected = AssertionError.class)
  public void runTestAction() {
    try {
      new TestAction<String, Text>() {
        @Override
        protected Text execute(String input, Object... other) {
          return new Text("hello");
        }

        @Override
        protected void verify(Text input, Object... outer) {
          assertEquals("Hello", "hello");
        }
      }.accept(new ActionRunner.Impl());
    } catch (AssertionError t) {
      assertEquals(
          "Expected: \"hello\"\n     got: \"Hello\"",
          t.getMessage());
      throw t;
    }
  }

  @Test
  public void forEachAndTestAction() {
    forEach(
        asList("a", "b"),
        new Block.Base<String>() {
          @Override
          public void apply(String input, Object... outer) {

          }
        }
    ).accept(new ActionRunner.Impl());
  }
}
