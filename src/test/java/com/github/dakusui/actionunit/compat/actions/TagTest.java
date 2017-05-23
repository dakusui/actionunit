package com.github.dakusui.actionunit.compat.actions;

import com.github.dakusui.actionunit.Action;
import org.junit.Test;

import static com.github.dakusui.actionunit.Actions.nop;
import static com.github.dakusui.actionunit.compat.CompatActions.tag;
import static junit.framework.TestCase.assertFalse;

public class TagTest {
  @Test
  public void givenNonTagAction$whenEquals$thenFalse() {
    Action action = tag(0);
    assertFalse(action.equals(nop()));
  }

  @Test
  public void givenTagActionWithDifferentIndex$whenEquals$thenFalse() {
    Action action = tag(0);
    assertFalse(action.equals(tag(1)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void givenNegativeInteger$whenInstantiate$thenIllegalArgumentThrown() {
    tag(-1);
  }
}
