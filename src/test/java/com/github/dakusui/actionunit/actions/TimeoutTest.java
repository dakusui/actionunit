package com.github.dakusui.actionunit.actions;

import com.github.dakusui.actionunit.Action;
import org.junit.Test;

import static com.github.dakusui.actionunit.Actions.nop;

public class TimeoutTest {
  @Test(expected = IllegalArgumentException.class)
  public void givenNegativeDuration$whenCreated$thenExceptionThrown() {
    new Action.TimeOut(nop(), -2);
  }
}
