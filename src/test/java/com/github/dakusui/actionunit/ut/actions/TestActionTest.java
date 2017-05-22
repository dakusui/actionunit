package com.github.dakusui.actionunit.ut.actions;

import com.github.dakusui.actionunit.Action;
import org.hamcrest.Matchers;
import org.junit.Test;

import static com.github.dakusui.actionunit.Actions.test;
import static com.github.dakusui.actionunit.Utils.describe;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.junit.MatcherAssert.assertThat;

public class TestActionTest {
  @Test
  public void givenTesAction() {
    Action action = test().when(input -> input).then(Matchers.anything()).build();

    assertThat(describe(action),
        allOf(
            containsString("Given"),
            containsString("When"),
            containsString("Then")
        )
    );
  }
}
