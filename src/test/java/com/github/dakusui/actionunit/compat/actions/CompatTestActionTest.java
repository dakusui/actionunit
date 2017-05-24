package com.github.dakusui.actionunit.compat.actions;

import com.github.dakusui.actionunit.core.Action;
import org.hamcrest.Matchers;
import org.junit.Test;

import static com.github.dakusui.actionunit.compat.CompatActions.test;
import static com.github.dakusui.actionunit.helpers.Utils.describe;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.junit.MatcherAssert.assertThat;

public class CompatTestActionTest {
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
