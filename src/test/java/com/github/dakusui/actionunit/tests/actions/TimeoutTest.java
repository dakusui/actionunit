package com.github.dakusui.actionunit.tests.actions;

import com.github.dakusui.actionunit.actions.TimeOut;
import org.junit.Test;

import static com.github.dakusui.actionunit.Actions.nop;

public class TimeoutTest {
  @Test(expected = IllegalArgumentException.class)
  public void givenNegativeDuration$whenCreated$thenExceptionThrown() {
    new TimeOut(nop(), -2);
  }
}
